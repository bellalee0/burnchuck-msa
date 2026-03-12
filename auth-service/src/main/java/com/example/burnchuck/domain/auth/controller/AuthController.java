package com.example.burnchuck.domain.auth.controller;

import static com.example.burnchuck.common.enums.SuccessMessage.AUTH_LOGIN_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.AUTH_LOGOUT_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.AUTH_NICKNAME_AVAILABLE;
import static com.example.burnchuck.common.enums.SuccessMessage.AUTH_REISSUE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.AUTH_SIGNUP_SUCCESS;

import com.example.burnchuck.common.dto.CommonResponse;
import com.example.burnchuck.common.enums.Provider;
import com.example.burnchuck.common.exception.CustomException;
import com.example.burnchuck.domain.auth.dto.request.AuthLoginRequest;
import com.example.burnchuck.domain.auth.dto.request.AuthSignupRequest;
import com.example.burnchuck.domain.auth.dto.response.AuthTokenResponse;
import com.example.burnchuck.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthService authService;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * 회원가입
     */
    @Operation(
            summary = "회원가입",
            description = """
                    필요한 정보들을 입력하여 새로운 사용자를 생성합니다.
                    """
    )
    @PostMapping("/signup")
    public ResponseEntity<CommonResponse<Void>> signup(
        @Valid @RequestBody AuthSignupRequest request,
        HttpServletResponse response
    ) {
        AuthTokenResponse authTokenResponse = authService.signup(request);

        addCookies(response, authTokenResponse);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CommonResponse.successNodata(AUTH_SIGNUP_SUCCESS));
    }

    /**
     * 로그인
     */
    @Operation(
            summary = "로그인",
            description = """
                    등록된 이메일과 비밀번호를 입력하여 토큰을 발급받습니다.
                    
                     - 토큰 만료 : 1시간
                    """
    )
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<Void>> login(
        @Valid @RequestBody AuthLoginRequest request,
        HttpServletResponse response
    ) {
        AuthTokenResponse authTokenResponse = authService.login(request);

        addCookies(response, authTokenResponse);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.successNodata(AUTH_LOGIN_SUCCESS));
    }

    @Operation(
        summary = "액세스 토큰 재발급",
        description = "Refresh 토큰을 기반으로 Access 토큰을 재발급합니다."
    )
    @PostMapping("/reissue")
    public ResponseEntity<CommonResponse<Void>> reissueToken(
            @CookieValue(name = "refreshToken") String refreshToken,
            HttpServletResponse response
    ) {
        AuthTokenResponse authTokenResponse = authService.reissueToken(refreshToken);

        addCookies(response, authTokenResponse);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.successNodata(AUTH_REISSUE_SUCCESS));
    }

    /**
     * 로그아웃 (쿠키 삭제)
     */
    @Operation(
        summary = "로그아웃",
        description = "쿠키를 만료시켜 로그아웃 처리합니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(HttpServletResponse response) {

        ResponseCookie atCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("None")
                .build();

        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, atCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return ResponseEntity.ok(CommonResponse.successNodata(AUTH_LOGOUT_SUCCESS));
    }

    /**
     * 닉네임 중복 확인
     */
    @GetMapping("/nickname-availability")
    public ResponseEntity<CommonResponse<Boolean>> checkNickname(
            @RequestParam String nickname
    ) {
        boolean isAvailable = authService.checkNicknameAvailable(nickname);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(AUTH_NICKNAME_AVAILABLE, isAvailable));
    }

    /**
     * 카카오 소셜 로그인
     */
    @Operation(
            summary = "카카오 소셜 로그인",
            description = """
                    인가 코드를 받아 로그인을 완료하고, 토큰을 쿠키에 담아 리다이렉트합니다.
                    """
    )
    @GetMapping("/kakao/callback")
    public void kakaoLogin(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {
        try {
            AuthTokenResponse authTokenResponse = authService.socialLogin(code, Provider.KAKAO);

            addCookies(response, authTokenResponse);

            response.sendRedirect(frontendUrl + "/oauth/callback");

        } catch (Exception e) {

            String message = "로그인 중 알 수 없는 오류가 발생했습니다.";

            if (e instanceof CustomException customException) {
                message = customException.getErrorCode().getMessage();
            }

            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

            response.sendRedirect(frontendUrl + "/login?error=true&message=" + encodedMessage);
        }
    }

    /**
     * 모든 메서드에서 공통으로 사용할 쿠키 생성기
     */
    private void addCookies(HttpServletResponse response, AuthTokenResponse tokenResponse) {

        String at = tokenResponse.getToken();
        String rt = tokenResponse.getRefreshToken();

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", at)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(3600)
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", rt)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(604800)
                .sameSite("None")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());
    }
}