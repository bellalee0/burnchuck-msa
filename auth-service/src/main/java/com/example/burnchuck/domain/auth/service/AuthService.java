package com.example.burnchuck.domain.auth.service;

import com.example.burnchuck.common.entity.Address;
import com.example.burnchuck.common.entity.User;
import com.example.burnchuck.common.entity.UserRefresh;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.enums.Gender;
import com.example.burnchuck.common.enums.Provider;
import com.example.burnchuck.common.enums.UserRole;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.common.utils.JwtUtil;
import com.example.burnchuck.domain.auth.dto.request.AuthLoginRequest;
import com.example.burnchuck.domain.auth.dto.request.AuthSignupRequest;
import com.example.burnchuck.domain.auth.dto.response.AuthTokenResponse;
import com.example.burnchuck.domain.auth.dto.response.KakaoUserInfoResponse;
import com.example.burnchuck.domain.auth.repository.AddressRepository;
import com.example.burnchuck.domain.auth.repository.UserRefreshRepository;
import com.example.burnchuck.domain.auth.repository.UserRepository;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserRefreshRepository userRefreshRepository;
    private final KakaoService kakaoService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @Transactional
    public AuthTokenResponse signup(AuthSignupRequest request) {

        String email = request.getEmail();
        String nickname = request.getNickname();

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.EMAIL_EXIST);
        }

        if (userRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.NICKNAME_EXIST);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        Gender gender = Gender.findEnum(request.getGender());

        Address address = addressRepository.findAddressByAddressInfo(request.getProvince(), request.getCity(), request.getDistrict());

        User user = new User(
                email, encodedPassword, nickname,
                request.getBirthDate(),
                gender,
                address,
                UserRole.USER,
                Provider.LOCAL,
                null
        );

        User savedUser = userRepository.saveAndFlush(user);

        return generateToken(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional
    public AuthTokenResponse login(AuthLoginRequest request) {

        User user = userRepository.findActivateUserByEmail(request.getEmail());

        boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!matches) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD);
        }

        return generateToken(user);
    }

    /**
     * 유저의 Access 토큰, Refresh 토큰 생성
     */
    private AuthTokenResponse generateToken(User user) {

        Long userId = user.getId();

        String accessToken = jwtUtil.generateAccessToken(userId, user.getEmail(), user.getNickname(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userId);

        UserRefresh userRefresh = userRefreshRepository.findByUserId(userId)
                .map(ur -> {
                    ur.updateRefreshToken(refreshToken);
                    return ur;
                })
                .orElseGet(() -> new UserRefresh(user, refreshToken));

        userRefreshRepository.save(userRefresh);

        return new AuthTokenResponse(accessToken, refreshToken);
    }

    /**
     * 토큰 재발급
     */
    @Transactional
    public AuthTokenResponse reissueToken(String refreshToken) {

        if (jwtUtil.isExpired(refreshToken)) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtUtil.extractId(refreshToken);

        UserRefresh userRefresh = userRefreshRepository.findUserRefreshByUserId(userId);

        if (!ObjectUtils.nullSafeEquals(refreshToken, userRefresh.getRefreshToken())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRefresh.getUser();

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getNickname(), user.getRole());

        if (jwtUtil.expireInTwoDays(refreshToken)) {
            refreshToken = jwtUtil.generateRefreshToken(userId);
            userRefresh.updateRefreshToken(refreshToken);
        }

        return new AuthTokenResponse(accessToken, refreshToken);
    }

    /**
     * 닉네임 중복 확인
     */
    public boolean checkNicknameAvailable(String nickname) {

        return !userRepository.existsByNickname(nickname);
    }


    /**
     * 소셜 로그인(유저 미 존재 시 회원가입)
     */
    @Transactional
    public AuthTokenResponse socialLogin(String code, Provider provider) {

        String accessToken = kakaoService.getKakaoAccessToken(code);
        KakaoUserInfoResponse userInfo = kakaoService.getKakaoUserInfo(accessToken);
        String providerId = String.valueOf(userInfo.getId());

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .map(this::checkUserStatus)
                .orElseGet(() -> createSocialUser(userInfo, provider));

        return generateToken(user);
    }

    /**
     * 유저 삭제 여부 확인
     */
    private User checkUserStatus(User user) {
        if (user.isDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }
        return user;
    }

    /**
     * 유저 생성(KAKAO)
     */
    private User createSocialUser(KakaoUserInfoResponse userInfo, Provider provider) {

        if (userRepository.existsByEmail(userInfo.getEmail())) {
            throw new CustomException(ErrorCode.EMAIL_EXIST);
        }

        String baseNickname = userInfo.getNickname();
        String tempPassword = passwordEncoder.encode(UUID.randomUUID().toString());
        Address defaultAddress = addressRepository.findById(1L)
                .orElseThrow(() -> new CustomException(ErrorCode.ADDRESS_NOT_FOUND));

        for (int i = 0; i < 5; i++) {
            String uniqueNickname = (i == 0) ? baseNickname : baseNickname + ThreadLocalRandom.current().nextInt(1000, 10000);

            if (!userRepository.existsByNickname(uniqueNickname)) {
                User newUser = new User(
                        userInfo.getEmail(),
                        tempPassword,
                        uniqueNickname,
                        null,
                        null,
                        defaultAddress,
                        UserRole.USER,
                        provider,
                        String.valueOf(userInfo.getId())
                );

                return userRepository.saveAndFlush(newUser);
            }
        }
        throw new CustomException(ErrorCode.NICKNAME_DUPLICATION_LIMIT_EXCEEDED);
    }
}