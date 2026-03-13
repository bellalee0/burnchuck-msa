package com.example.burnchuck.domain.user.service;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.entity.Address;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.event.user.UserEventPublisher;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.domain.follow.repository.FollowRepository;
import com.example.burnchuck.domain.user.dto.S3UrlResponse;
import com.example.burnchuck.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.burnchuck.domain.user.dto.request.UserUpdateProfileRequest;
import com.example.burnchuck.domain.user.dto.response.UserGetAddressResponse;
import com.example.burnchuck.domain.user.dto.response.UserGetOneResponse;
import com.example.burnchuck.domain.user.dto.response.UserGetProfileResponse;
import com.example.burnchuck.domain.user.dto.response.UserUpdateProfileResponse;
import com.example.burnchuck.domain.user.repository.AddressRepository;
import com.example.burnchuck.domain.user.repository.ReviewRepository;
import com.example.burnchuck.domain.user.repository.UserRepository;
import com.example.burnchuck.domain.user.util.S3UrlGenerator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final FollowRepository followRepository;
    private final ReviewRepository reviewRepository;

    private final UserEventPublisher userEventPublisher;

    private final PasswordEncoder passwordEncoder;
    private final S3UrlGenerator s3UrlGenerator;

    /**
     * 프로필 이미지 업로드 Presigned URL 생성
     */
    public S3UrlResponse getUploadProfileImgUrl(AuthUser authUser, String filename) {

        String key = "profile/" + authUser.getId() + "/" + UUID.randomUUID();
        return s3UrlGenerator.generateUploadImgUrl(filename, key);
    }

    /**
     * 프로필 이미지 등록
     */
    @Transactional
    public S3UrlResponse getViewProfileImgUrl(AuthUser authUser, String key) {

        s3UrlGenerator.validateKeyOwnership(authUser.getId(), key);

        if (!s3UrlGenerator.isFileExists(key)) {
            throw new CustomException(ErrorCode.USER_IMG_NOT_FOUND);
        }

        User user = userRepository.findActivateUserById(authUser.getId());

        S3UrlResponse result = s3UrlGenerator.generateViewImgUrl(key);

        user.uploadProfileImg(result.getPreSignedUrl());
        userRepository.save(user);

        return result;
    }

    /**
     * 내 정보 수정(닉네임, 주소, 프로필)
     */
    @Transactional
    public UserUpdateProfileResponse updateProfile(AuthUser authUser, UserUpdateProfileRequest request) {

        String requestedProfileImgUrl = request.getProfileImgUrl();

        if (requestedProfileImgUrl != null && !s3UrlGenerator.isFileExists(requestedProfileImgUrl.replaceAll("^https?://[^/]+/", ""))) {
            throw new CustomException(ErrorCode.USER_IMG_NOT_FOUND);
        }

        User user = userRepository.findActivateUserById(authUser.getId());

        String currentNickname = user.getNickname();
        String newNickname = request.getNickname();

        boolean isNicknameChanged = !ObjectUtils.nullSafeEquals(currentNickname, newNickname);
        boolean existNickname = userRepository.existsByNickname(newNickname);

        if (isNicknameChanged && existNickname) {
            throw new CustomException(ErrorCode.NICKNAME_EXIST);
        }

        Address newAddress = addressRepository.findAddressByAddressInfo(
            request.getProvince(),
            request.getCity(),
            request.getDistrict()
        );

        user.updateProfile(request.getProfileImgUrl(), newNickname, newAddress);
        userRepository.saveAndFlush(user);

        return UserUpdateProfileResponse.from(user, newAddress);
    }

    /**
     * 비밀번호 변경
     */
    @Transactional
    public void updatePassword(AuthUser authUser, UserUpdatePasswordRequest request) {

        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        if (ObjectUtils.nullSafeEquals(oldPassword, newPassword)) {
            throw new CustomException(ErrorCode.SAME_PASSWORD);
        }

        User user = userRepository.findActivateUserById(authUser.getId());

        boolean oldPasswordMatches = passwordEncoder.matches(oldPassword, user.getPassword());
        if (!oldPasswordMatches) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        boolean newPasswordMatches = passwordEncoder.matches(newPassword, user.getPassword());
        if (newPasswordMatches) {
            throw new CustomException(ErrorCode.SAME_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(newPassword);

        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void deleteUser(AuthUser authUser) {

        User user = userRepository.findActivateUserById(authUser.getId());

        followRepository.deleteByFollowerId(user.getId());
        followRepository.deleteByFolloweeId(user.getId());

        user.delete();
        userRepository.saveAndFlush(user);

        userEventPublisher.publishUserDeletedEvent(user.getId());
    }

    /**
     * 프로필 조회
     */
    @Transactional(readOnly = true)
    public UserGetProfileResponse getProfile(Long userId) {

        User user = userRepository.findActivateUserById(userId);

        Long followings = followRepository.countByFollower(user);
        Long followers = followRepository.countByFollowee(user);

        Double avgRates = reviewRepository.findAvgRatesByReviewee(user);
        avgRates = avgRates == null ? 0 : avgRates;

        return new UserGetProfileResponse(
            user.getProfileImgUrl(),
            user.getNickname(),
            followings,
            followers,
            avgRates
        );
    }

    /**
     * 주소 조회
     */
    @Transactional(readOnly = true)
    public UserGetAddressResponse getAddress(AuthUser authUser) {

        User user = userRepository.findActivateUserWithAddress(authUser.getId());

        return UserGetAddressResponse.from(user.getAddress());
    }

    /**
     * 유저 단건 조회
     */
    @Transactional(readOnly = true)
    public UserGetOneResponse getUserInfo(AuthUser authUser) {

        return new UserGetOneResponse(authUser.getId(), authUser.getEmail(), authUser.getNickname(), authUser.getUserRole());
    }
}
