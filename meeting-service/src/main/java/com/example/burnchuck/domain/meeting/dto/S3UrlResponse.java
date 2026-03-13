package com.example.burnchuck.domain.meeting.dto;

import lombok.*;

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