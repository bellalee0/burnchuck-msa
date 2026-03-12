package com.example.burnchuck.domain.meeting.controller;

import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_CREATE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_DELETE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_GET_HOSTED_LIST_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_GET_MEMBER_LIST_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_GET_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_IMG_UPLOAD_LINK_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.MEETING_UPDATE_SUCCESS;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.dto.CommonResponse;
import com.example.burnchuck.common.dto.S3UrlResponse;
import com.example.burnchuck.common.dto.PageResponse;
import com.example.burnchuck.common.enums.MeetingSortOption;
import com.example.burnchuck.domain.meeting.dto.request.LocationFilterRequest;
import com.example.burnchuck.domain.meeting.dto.request.MeetingCreateRequest;
import com.example.burnchuck.domain.meeting.dto.request.MeetingMapViewPortRequest;
import com.example.burnchuck.domain.meeting.dto.request.MeetingSearchRequest;
import com.example.burnchuck.domain.meeting.dto.request.MeetingUpdateRequest;
import com.example.burnchuck.domain.meeting.dto.request.UserLocationRequest;
import com.example.burnchuck.domain.meeting.dto.response.MeetingCreateResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingDetailResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingMapPointResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingMemberResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingSummaryResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingSummaryWithStatusResponse;
import com.example.burnchuck.domain.meeting.dto.response.MeetingUpdateResponse;
import com.example.burnchuck.domain.meeting.service.MeetingSearchService;
import com.example.burnchuck.domain.meeting.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/meetings")
@Tag(name = "Meeting")
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingSearchService meetingSearchService;

    /**
     * 모임 이미지 업로드 Presigned URL 생성
     */
    @GetMapping("/img")
    public ResponseEntity<CommonResponse<S3UrlResponse>> getUploadImgUrl(
            @RequestParam String filename
    ) {
        S3UrlResponse response = meetingService.getUploadMeetingImgUrl(filename);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(MEETING_IMG_UPLOAD_LINK_SUCCESS, response));
    }

    /**
     * 모임 생성
     */
    @Operation(
            summary = "모임 생성",
            description = """
                    번개 모임을 생성합니다.
                    """
    )
    @PostMapping
    public ResponseEntity<CommonResponse<MeetingCreateResponse>> createMeeting(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody MeetingCreateRequest request
    ) {
        MeetingCreateResponse response = meetingService.createMeeting(user, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(MEETING_CREATE_SUCCESS, response));
    }

    /**
     * 모임 목록 조회
     */
    @Operation(
            summary = "모임 목록 조회",
            description = """
                    현재 모집 중인 번개 모임을 사용자 지정 조건에 따라 목록 형태로 조회합니다.
                    """
    )
    @GetMapping
    public ResponseEntity<CommonResponse<PageResponse<MeetingSummaryResponse>>> getMeetingPage(
            @AuthenticationPrincipal AuthUser authUser,
            @ModelAttribute MeetingSearchRequest searchRequest,
            @ModelAttribute LocationFilterRequest locationRequest,
            @ModelAttribute UserLocationRequest userLocationRequest,
            @RequestParam(required = false) MeetingSortOption order,
            @PageableDefault(size = 6) Pageable pageable
    ) {
        PageResponse<MeetingSummaryResponse> response = meetingSearchService.getMeetingPage(authUser, searchRequest, locationRequest, userLocationRequest, order, pageable);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(MEETING_GET_SUCCESS, response));
    }

    /**
     * 모임 지도 조회
     */
    @Operation(
        summary = "모임 지도 조회",
        description = """
                    현재 모집 중인 번개 모임을 사용자 화면과 지정 조건에 따라 지도 형태로 조회합니다.
                    """
    )
    @GetMapping("/map")
    public ResponseEntity<CommonResponse<List<MeetingMapPointResponse>>> getMeetingPointList(
        @ModelAttribute MeetingSearchRequest searchRequest,
        @ModelAttribute MeetingMapViewPortRequest viewPort
    ) {
        List<MeetingMapPointResponse> pointList = meetingSearchService.getMeetingPointList(searchRequest, viewPort);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(MEETING_GET_SUCCESS, pointList));
    }

    /**
     * 모임 단건 요약 조회
     */
    @Operation(
        summary = "모임 단건 요약 조회",
        description = """
                    특정 모임의 요약된 내용을 조회합니다.
                    """
    )
    @GetMapping("/{meetingId}/summary")
    public ResponseEntity<CommonResponse<MeetingSummaryResponse>> getMeetingSummary(
        @PathVariable Long meetingId
    ) {
        MeetingSummaryResponse response = meetingService.getMeetingSummary(meetingId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(MEETING_GET_SUCCESS, response));
    }

    /**
     * 모임 단건 조회
     */
    @Operation(
            summary = "모임 단건 조회",
            description = """
                    특정 모임의 세부 내용을 조회합니다.
                    """
    )
    @GetMapping("/{meetingId}")
    public ResponseEntity<CommonResponse<MeetingDetailResponse>> getMeetingDetail(
            @PathVariable Long meetingId,
            HttpServletRequest httpServletRequest
    ) {
        MeetingDetailResponse response = meetingService.getMeetingDetail(meetingId, httpServletRequest);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(MEETING_GET_SUCCESS, response));
    }

    /**
     * 모임 삭제
     */
    @Operation(
            summary = "모임 삭제",
            description = """
                    번개 모임을 삭제합니다.
                    """
    )
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<CommonResponse<Void>> deleteMeeting(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long meetingId
    ) {
        meetingService.deleteMeeting(authUser, meetingId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(CommonResponse.successNodata(MEETING_DELETE_SUCCESS));
    }

    /**
     * 모임 수정
     */
    @Operation(
            summary = "모임 수정",
            description = """
                    번개 모임의 정보를 수정합니다.
                    """
    )
    @PatchMapping("/{meetingId}")
    public ResponseEntity<CommonResponse<MeetingUpdateResponse>> updateMeeting(
            @AuthenticationPrincipal AuthUser user,
            @PathVariable Long meetingId,
            @Valid @RequestBody MeetingUpdateRequest request
    ) {
        MeetingUpdateResponse response = meetingService.updateMeeting(user, meetingId, request);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(MEETING_UPDATE_SUCCESS, response));
    }

    /**
     * 주최한 모임 목록 조회 (로그인한 유저 기준)
     */
    @Operation(
            summary = "주최한 모임 목록 조회 (로그인한 유저 기준)",
            description = """
                    로그인한 유저가 주최한 모임 목록을 조회합니다.
                    """
    )
    @GetMapping("/hosted-meetings")
    public ResponseEntity<CommonResponse<PageResponse<MeetingSummaryWithStatusResponse>>> getMyHostedMeetings(
        @AuthenticationPrincipal AuthUser authUser,
        @PageableDefault(size = 6) Pageable pageable
    ) {
        Page<MeetingSummaryWithStatusResponse> page = meetingService.getMyHostedMeetings(authUser, pageable);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(MEETING_GET_HOSTED_LIST_SUCCESS, PageResponse.from(page)));
    }

    /**
     * 주최한 모임 목록 조회 (입력받은 유저 기준)
     */
    @Operation(
            summary = "주최한 모임 목록 조회 (입력받은 유저 기준)",
            description = """
                    특정 사용자가 주최한 모임 목록을 조회합니다.
                    """
    )
    @GetMapping("/hosted-meetings/users/{userId}")
    public ResponseEntity<CommonResponse<PageResponse<MeetingSummaryWithStatusResponse>>> getOthersHostedMeetings(
        @PathVariable Long userId,
        @PageableDefault(size = 6) Pageable pageable
    ) {
        Page<MeetingSummaryWithStatusResponse> page = meetingService.getOthersHostedMeetings(userId, pageable);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(MEETING_GET_HOSTED_LIST_SUCCESS, PageResponse.from(page)));
    }

    /**
     * 모임 참여자 목록 조회
     */
    @Operation(
            summary = "모임 참여자 목록 조회",
            description = """
                    특정 모임에 참여하는 사람들을 조회합니다.
                    """
    )
    @GetMapping("/{meetingId}/attendees")
    public ResponseEntity<CommonResponse<MeetingMemberResponse>> getMeetingMembers(
        @PathVariable Long meetingId
    ) {
        MeetingMemberResponse response = meetingService.getMeetingMembers(meetingId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(MEETING_GET_MEMBER_LIST_SUCCESS, response));
    }
}
