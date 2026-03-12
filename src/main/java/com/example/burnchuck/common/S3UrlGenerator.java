package com.example.burnchuck.common;

import com.example.burnchuck.common.dto.S3UrlResponse;
import com.example.burnchuck.common.enums.ErrorCode;
import com.example.burnchuck.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class S3UrlGenerator {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.cloudfront.domain}")
    private String cloudFrontDomain;

    // 허용된 파일 확장자
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg",
            "jpeg",
            "png"
    );

    /**
     * 이미지 업로드용 Presigned URL, 조회용 CloudFront URL 생성
     */
    public S3UrlResponse generateUploadImgUrl(String filename, String key) {

        if (!validateFileType(filename)) {
            throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
        }

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(getContentType(filename))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(putObjectRequest)
                .build();

        String preSignedUrl = s3Presigner
                .presignPutObject(presignRequest)
                .url()
                .toExternalForm();

        String publicUrl = cloudFrontDomain + "/" + key;

        return S3UrlResponse.builder()
                .preSignedUrl(preSignedUrl)
                .cloudFrontUrl(publicUrl)
                .key(key)
                .build();
    }

    /**
     * 이미지 조회 CloudFront 링크 생성
     */
    public S3UrlResponse generateViewImgUrl(String key) {
        String publicUrl = cloudFrontDomain + "/" + key;

        return S3UrlResponse.builder()
                .preSignedUrl(publicUrl)
                .key(key)
                .build();
    }

    /**
     * S3에 이미지 파일 존재 여부 확인
     */
    public boolean isFileExists(String key) {
        try {
            s3Client.headObject(
                    HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (SdkException e) {
            throw new CustomException(ErrorCode.S3_INTERNAL_ERROR);
        }
    }

    /**
     * Key 소유권 검증
     */
    public void validateKeyOwnership(Long userId, String key) {
        if (!key.startsWith("profile/" + userId + "/")) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_IMAGE_ACCESS);
        }
    }

    /**
     * 파일 형식 검증
     */
    private boolean validateFileType(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex < 0) {
            return false;
        }

        String extension = filename.substring(lastDotIndex + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }

    /**
     * Content-Type 결정
     */
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}
