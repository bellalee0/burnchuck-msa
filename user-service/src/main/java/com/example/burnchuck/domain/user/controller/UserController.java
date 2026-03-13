package com.example.burnchuck.domain.user.controller;

import static com.example.burnchuck.common.enums.SuccessMessage.USER_DELETE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_GET_ADDRESS_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_GET_ONE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_GET_PROFILE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_UPDATE_PASSWORD_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_UPDATE_PROFILE_IMG_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_UPDATE_PROFILE_SUCCESS;
import static com.example.burnchuck.common.enums.SuccessMessage.USER_UPLOAD_PROFILE_IMG_LINK_SUCCESS;

import com.example.burnchuck.common.dto.AuthUser;
import com.example.burnchuck.common.dto.CommonResponse;
import com.example.burnchuck.domain.user.dto.S3UrlResponse;
import com.example.burnchuck.domain.user.dto.request.UserUpdatePasswordRequest;
import com.example.burnchuck.domain.user.dto.request.UserUpdateProfileRequest;
import com.example.burnchuck.domain.user.dto.response.UserGetAddressResponse;
import com.example.burnchuck.domain.user.dto.response.UserGetOneResponse;
import com.example.burnchuck.domain.user.dto.response.UserGetProfileResponse;
import com.example.burnchuck.domain.user.dto.response.UserUpdateProfileResponse;
import com.example.burnchuck.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User")
public class UserController {

    private final UserService userService;

    /**
     * 프로필 이미지 업로드 Presigned URL 생성
     */
    @GetMapping("/profileImg")
    public ResponseEntity<CommonResponse<S3UrlResponse>> getUploadImgUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String filename
    ) {
        S3UrlResponse response = userService.getUploadProfileImgUrl(authUser, filename);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(USER_UPLOAD_PROFILE_IMG_LINK_SUCCESS, response));
    }

    /**
     * 프로필 이미지 등록
     */
    @PatchMapping("/profileImg")
    public ResponseEntity<CommonResponse<S3UrlResponse>> getViewImgUrl(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam String key
    ) {
        S3UrlResponse response = userService.getViewProfileImgUrl(authUser, key);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(USER_UPDATE_PROFILE_IMG_SUCCESS, response));
    }

    /**
     * 내 정보 수정(프로필 이미지, 닉네임, 주소)
     */
    @Operation(
            summary = "내 정보 수정",
            description = """
                    나의 닉네임과 주소, 프로필 이미지를 수정할 수 있습니다.
                    """
    )
    @PatchMapping
    public ResponseEntity<CommonResponse<UserUpdateProfileResponse>> updateProfile(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody UserUpdateProfileRequest request
    ) {
        UserUpdateProfileResponse response = userService.updateProfile(authUser, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(USER_UPDATE_PROFILE_SUCCESS, response));
    }

    /**
     * 비밀번호 변경
     */
    @Operation(
            summary = "비밀번호 변경",
            description = """
                    이전 비밀번호를 통해 새 비밀번호로 변경할 수 있습니다.
                    """
    )
    @PutMapping("/password")
    public ResponseEntity<CommonResponse<Void>> updatePassword(
        @AuthenticationPrincipal AuthUser authUser,
        @Valid @RequestBody UserUpdatePasswordRequest request
    ) {
        userService.updatePassword(authUser, request);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.successNodata(USER_UPDATE_PASSWORD_SUCCESS));
    }

    /**
     * 회원 탈퇴
     */
    @Operation(
            summary = "회원 탈퇴",
            description = """
                    해당 계정을 탈퇴합니다.
                    탈퇴한 계정은 논리적으로 삭제됩니다.
                    """
    )
    @DeleteMapping
    public ResponseEntity<CommonResponse<Void>> deleteUser(
        @AuthenticationPrincipal AuthUser authUser,
        HttpServletResponse response
    ) {
        userService.deleteUser(authUser);

        ResponseCookie atCookie = ResponseCookie.from("accessToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        ResponseCookie rtCookie = ResponseCookie.from("refreshToken", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, atCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return ResponseEntity.status(HttpStatus.NO_CONTENT)
            .body(CommonResponse.successNodata(USER_DELETE_SUCCESS));
    }

    /**
     * 프로필 조회
     */
    @Operation(
            summary = "프로필 조회",
            description = """
                    특정 사용자의 프로필을 조회할 수 있습니다.
                    """
    )
    @GetMapping("/{userId}")
    public ResponseEntity<CommonResponse<UserGetProfileResponse>> getProfile(
        @PathVariable Long userId
    ) {
        UserGetProfileResponse response = userService.getProfile(userId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(USER_GET_PROFILE_SUCCESS, response));
    }

    /**
     * 주소 조회
     */
    @Operation(
        summary = "주소 조회",
        description = """
                    로그인한 사용자의 주소을 조회할 수 있습니다.
                    """
    )
    @GetMapping("/address")
    public ResponseEntity<CommonResponse<UserGetAddressResponse>> getAddress(
        @AuthenticationPrincipal AuthUser authUser
    ) {
        UserGetAddressResponse response = userService.getAddress(authUser);

        return ResponseEntity.status(HttpStatus.OK)
            .body(CommonResponse.success(USER_GET_ADDRESS_SUCCESS, response));
    }

    /**
     * 유저 단건 조회
     */
    @GetMapping
    public ResponseEntity<CommonResponse<UserGetOneResponse>> getUserInfo(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        UserGetOneResponse response = userService.getUserInfo(authUser);

        return ResponseEntity.status(HttpStatus.OK)
                .body(CommonResponse.success(USER_GET_ONE_SUCCESS, response));
    }
}
