package com.epam.finaltask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @NotBlank(message = "{user.password.invalid}")
    private String currentPassword;

    @Pattern(
            regexp = "^.{5,}$",
            message = "{user.password.invalid}"
    )
    private String newPassword;

    @NotBlank(message = "{user.confirmPassword.required}")
    private String confirmPassword;
}