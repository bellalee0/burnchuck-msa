package com.example.burnchuck.domain.auth.dto.request;

import com.example.burnchuck.common.constants.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailConfirmRequest {

    @NotBlank(message = ValidationMessage.EMAIL_NOT_BLANK)
    @Email(message = ValidationMessage.EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ValidationMessage.VERIFICATION_CODE_NOT_BLANK)
    private String verificationCode;
}
