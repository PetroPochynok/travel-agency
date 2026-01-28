package com.epam.finaltask.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9]{2,}$",
            message = "{user.username.invalid}"
    )
    private String username;

    @Pattern(
            regexp = "^[A-Za-z]{2,}$",
            message = "{user.firstname.invalid}"
    )
    private String firstName;

    @Pattern(
            regexp = "^[A-Za-z]{2,}$",
            message = "{user.lastname.invalid}"
    )
    private String lastName;

    @Pattern(
            regexp = "^.{5,}$",
            message = "{user.password.invalid}"
    )
    private String password;

    @NotBlank(message = "{user.confirmPassword.required}")
    private String confirmPassword;

    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
            message = "{user.email.invalid}"
    )
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "{user.phone.invalid}"
    )
    private String phoneNumber;
}