package com.example.burnchuck.domain.auth.dto.request;

import com.example.burnchuck.common.constants.ValidationMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthSignupRequest {

    @NotBlank(message = ValidationMessage.EMAIL_NOT_BLANK)
    @Email(message = ValidationMessage.EMAIL_FORMAT)
    private String email;

    @NotBlank(message = ValidationMessage.PASSWORD_NOT_BLANK)
    private String password;

    @NotBlank(message = ValidationMessage.USERNAME_NOT_BLANK)
    @Size(max = 50, message = ValidationMessage.USERNAME_SIZE)
    private String nickname;

    @NotNull(message = ValidationMessage.BIRTHDATE_NOT_BLANK)
    private LocalDate birthDate;

    @NotBlank(message = ValidationMessage.PROVINCE_NOT_BLANK)
    private String province;

    @NotBlank(message = ValidationMessage.CITY_NOT_BLANK)
    private String city;

    private String district;

    @NotBlank(message = ValidationMessage.GENDER_NOT_BLANK)
    @Pattern(regexp = "^(남|여)$", message = ValidationMessage.GENDER_PATTERN)
    private String gender;
}
