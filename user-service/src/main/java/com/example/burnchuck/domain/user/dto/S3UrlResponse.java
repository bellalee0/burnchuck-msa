package com.example.burnchuck.domain.user.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class S3UrlResponse {

    private String preSignedUrl;

    private String cloudFrontUrl;

    private String key;

    @Builder
    public S3UrlResponse(String preSignedUrl, String cloudFrontUrl, String key) {
        this.preSignedUrl = preSignedUrl;
        this.cloudFrontUrl = cloudFrontUrl;
        this.key = key;
    }
}