package com.example.burnchuck.domain.chat.service;

import static com.example.burnchuck.common.enums.ErrorCode.CANNOT_CHAT_WITH_SELF;
import static com.example.burnchuck.common.enums.ErrorCode.CANNOT_LEAVE_NOT_COMPLETED_MEETING;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.entity.ChatRoom;
import com.example.burnchuck.common.entity.ChatRoomUser;
import com.example.burnchuck.common.entity.Meeting;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.enums.RoomType;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.common.utils.UserDisplay;
import com.example.burnchuck.domain.chat.dto.dto.ChatRoomCreationResult;
import com.example.burnchuck.domain.chat.dto.dto.ChatRoomDto;
import com.example.burnchuck.domain.chat.dto.dto.ChatRoomMemberDto;
import com.example.burnchuck.domain.chat.dto.request.ChatRoomNameUpdateRequest;
import com.example.burnchuck.domain.chat.dto.response.ChatMessageResponse;
import com.example.burnchuck.domain.chat.dto.response.ChatRoomDetailResponse;
import com.example.burnchuck.domain.chat.repository.ChatMessageRepository;
import com.example.burnchuck.domain.chat.repository.ChatRoomRepository;
import com.example.burnchuck.domain.chat.repository.ChatRoomUserRepository;
import com.example.burnchuck.domain.chat.repository.MeetingRepository;
import com.example.burnchuck.domain.chat.repository.UserRepository;
import com.example.burnchuck.domain.entity.ChatMessage;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;

    private final ChatCacheService chatCacheService;

    /**
     * 1:1 채팅방 생성 (이미 존재하면 기존 방 ID 반환)
     */
    @Transactional
    public ChatRoomCreationResult getOrCreatePrivateRoom(AuthUser authUser, Long targetUserId) {

        User me = userRepository.findActivateUserById(authUser.getId());
        User target = userRepository.findActivateUserById(targetUserId);

        if (ObjectUtils.nullSafeEquals(me.getId(), target.getId())) {
            throw new CustomException(CANNOT_CHAT_WITH_SELF);
        }

        return chatRoomRepository.findPrivateChatRoom(me.getId(), target.getId())
                .map(chatRoom -> new ChatRoomCreationResult(chatRoom.getId(), false))
                .orElseGet(() -> {
                    Long newRoomId = createPrivateChatRoom(me, target);
                    return new ChatRoomCreationResult(newRoomId, true);
                });
    }

    /**
     * 1:1 채팅방 생성 로직
     */
    private Long createPrivateChatRoom(User me, User target) {

        String roomName = me.getNickname() + ", " + target.getNickname();

        ChatRoom room = new ChatRoom(roomName, RoomType.PRIVATE);
        chatRoomRepository.save(room);

        chatRoomUserRepository.save(new ChatRoomUser(room, me));
        chatRoomUserRepository.save(new ChatRoomUser(room, target));

        chatCacheService.updateLastReadSequence(room.getId(), me.getId(), 0L);
        chatCacheService.updateLastReadSequence(room.getId(), target.getId(), 0L);

        return room.getId();
    }

    /**
     * 그룹 채팅방 생성
     * 모임 생성시 자동 호출
     */
    @Transactional
    public void createGroupChatRoom(Meeting meeting, User host) {

        ChatRoom chatRoom = new ChatRoom(meeting.getTitle(), RoomType.GROUP, meeting.getId());
        chatRoomRepository.save(chatRoom);

        ChatRoomUser chatRoomUser = new ChatRoomUser(chatRoom, host);
        chatRoomUserRepository.save(chatRoomUser);
    }

    /**
     * 그룹 채팅방 입장 (모임 참여 시 호출)
     */
    @Transactional
    public void joinGroupChatRoom(Long meetingId, User user) {

        ChatRoom chatRoom = chatRoomRepository.findChatRoomByMeetingId(meetingId);

        boolean isAlreadyMember = chatRoomUserRepository.existsByChatRoomAndUser(chatRoom, user);
        if (isAlreadyMember) {
            return;
        }

        ChatRoomUser chatRoomUser = new ChatRoomUser(chatRoom, user);
        chatRoomUserRepository.save(chatRoomUser);

        Long currentSeq = chatCacheService.getRoomCurrentSequence(chatRoom.getId());
        chatCacheService.updateLastReadSequence(chatRoom.getId(), user.getId(), currentSeq);
    }

    /**
     * 내 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getMyChatRooms(AuthUser authUser) {

        User user = userRepository.findActivateUserById(authUser.getId());

        List<ChatRoomUser> myRoomUsers = chatRoomUserRepository.findAllActiveByUserId(user.getId());

        List<Long> roomIdList = myRoomUsers.stream()
                .map(roomUser -> roomUser.getChatRoom().getId())
                .collect(Collectors.toList());

        Map<Long, Long> unreadCounts = chatCacheService.getUnreadCountsBatch(user.getId(), roomIdList);

        return myRoomUsers.stream()
                .map(myRoomUser -> {
                    Long roomId = myRoomUser.getChatRoom().getId();
                    Long count = unreadCounts.getOrDefault(roomId, 0L);
                    return convertToChatRoomDto(myRoomUser, user.getId(), count);
                })
                .collect(Collectors.toList());
    }

    /**
     * DTO 변환 로직
     */
    private ChatRoomDto convertToChatRoomDto(ChatRoomUser myRoomUser, Long loginUserId, Long unreadCount) {

        ChatRoom chatRoom = myRoomUser.getChatRoom();

        String roomName = myRoomUser.getCustomRoomName();
        if (roomName == null) {
            roomName = chatRoom.getName();
        }

        String chatroomImg = null;

        if (chatRoom.isPrivate()) {
            User user = getPartner(chatRoom, loginUserId);
            roomName = UserDisplay.resolveNickname(user);
            chatroomImg = UserDisplay.resolveProfileImg(user);
        }

        if (chatRoom.isGroup()){
            chatroomImg = meetingRepository.findByIdAndDeletedFalse(chatRoom.getMeetingId())
                    .map(Meeting::getImgUrl)
                    .orElse(null);
        }

        ChatMessage lastMsg = chatMessageRepository.findFirstByRoomIdOrderByCreatedDatetimeDesc(chatRoom.getId())
                .orElse(null);

        int memberCount = chatRoomUserRepository.countByChatRoomId(chatRoom.getId());

        return ChatRoomDto.of(chatRoom, roomName, lastMsg, chatroomImg, memberCount, unreadCount);
    }

    /**
     * 채팅 내역 조회 (무한 스크롤)
     */
    @Transactional(readOnly = true)
    public Slice<ChatMessageResponse> getChatMessages(Long roomId, Pageable pageable) {

        boolean exists = chatRoomRepository.existsById(roomId);
        if (!exists) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        return chatMessageRepository.findByRoomIdOrderByCreatedDatetimeDesc(roomId, pageable)
                .map(ChatMessageResponse::from);
    }

    /**
     * 채팅방 나가기
     */
    @Transactional
    public void leaveChatRoom(AuthUser authUser, Long roomId) {

        User user = userRepository.findActivateUserById(authUser.getId());

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findChatRoomUserByChatRoomIdAndUserId(roomId, user.getId());

        ChatRoom room = chatRoomUser.getChatRoom();

        if (room.isGroup()) {

            Meeting meeting = meetingRepository.findActivateMeetingById(room.getMeetingId());

            if (!meeting.isDeleted() && !meeting.isCompleted()) {
                throw new CustomException(CANNOT_LEAVE_NOT_COMPLETED_MEETING);
            }
        }

        chatRoomUser.delete();
        chatCacheService.deleteUserReadInfo(roomId, user.getId());
    }

    /**
     * 모임 참가 취소 / 유저 탈퇴 시, 채팅방 나가기
     */
    @Transactional
    public void leaveChatRoomRegardlessOfStatus(Long meetingId, Long userId) {

        ChatRoom chatRoom = chatRoomRepository.findChatRoomByMeetingId(meetingId);
        Long roomId = chatRoom.getId();

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findChatRoomUserByChatRoomIdAndUserId(roomId, userId);

        chatRoomUser.delete();
        chatCacheService.deleteUserReadInfo(roomId, userId);
    }

    /**
     * 상대 유저 조회
     */
    private User getPartner(ChatRoom room, Long myId) {

        return chatRoomUserRepository.findByChatRoomId(room.getId()).stream()
                .map(ChatRoomUser::getUser)
                .filter(user -> !user.getId().equals(myId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CHAT_PARTNER_NOT_FOUND));
    }

    /**
     * 채팅방 이름 수정
     */
    @Transactional
    public void updateRoomName(AuthUser authUser, Long roomId, ChatRoomNameUpdateRequest request) {

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findChatRoomUserByChatRoomIdAndUserId(roomId, authUser.getId());

        chatRoomUser.updateCustomName(request.getName());
    }

    /**
     * 채팅방 단건 조회 (참여자 목록 포함)
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(AuthUser authUser, Long roomId) {

        User user = userRepository.findActivateUserById(authUser.getId());

        ChatRoomUser myRoomUser = chatRoomUserRepository.findChatRoomUserByChatRoomIdAndUserId(roomId, user.getId());

        ChatRoom room = myRoomUser.getChatRoom();

        String roomName = myRoomUser.getCustomRoomName();
        if (roomName == null) {
            roomName = room.getName();
        }

        if (room.isPrivate()) {
            User partner = getPartner(room, user.getId());
            roomName = UserDisplay.resolveNickname(partner);
        }

        List<ChatRoomUser> roomUsers = chatRoomUserRepository.findByChatRoomId(roomId);

        List<ChatRoomMemberDto> members = roomUsers.stream()
                .map(roomUser -> ChatRoomMemberDto.from(roomUser.getUser()))
                .toList();

        List<Long> memberIdList = roomUsers.stream()
                .map(ru -> ru.getUser().getId())
                .toList();

        Map<Long, Long> readStatuses = chatCacheService.getMembersLastReadSequence(roomId, memberIdList);

        return ChatRoomDetailResponse.from(room, roomName, members, readStatuses);
    }
}
