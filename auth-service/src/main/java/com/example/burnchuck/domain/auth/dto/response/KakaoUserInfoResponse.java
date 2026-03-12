package com.example.burnchuck.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponse {

    private Long id; // 카카오 고유 ID(providerId)

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {

        private String email;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {

            private String nickname;
        }
    }

    public String getEmail() {

        if (kakaoAccount == null){

            return null;

        } else {

            return kakaoAccount.getEmail();
        }
    }

    public String getNickname() {

        if (kakaoAccount == null){

            return null;
        }

        if (kakaoAccount.getProfile() == null) {

            return null;
        }

        return kakaoAccount.getProfile().getNickname();
    }
}
