package com.epam.finaltask.dto;

import com.epam.finaltask.annotation.Sensitive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "{user.password.invalid}")
    @Sensitive
    private String currentPassword;

    @Pattern(
            regexp = "^.{5,}$",
            message = "{user.password.invalid}"
    )
    @Sensitive
    private String newPassword;

    @NotBlank(message = "{user.confirmPassword.required}")
    @Sensitive
    private String confirmPassword;
}