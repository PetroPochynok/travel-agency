package com.epam.finaltask.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @Pattern(
            regexp = "^[A-Za-z][A-Za-z0-9]{2,}$",
            message = "Username must start with a letter and be at least 3 characters"
    )
    private String username;

    @Pattern(
            regexp = "^[A-Za-z]{2,}$",
            message = "Must contain only letters and be at least 2 characters"
    )
    private String firstName;

    @Pattern(
            regexp = "^[A-Za-z]{2,}$",
            message = "Must contain only letters and be at least 2 characters"
    )
    private String lastName;

    @Pattern(
            regexp = "^.{5,}$",
            message = "Password must be at least 5 characters"
    )
    private String password;

    @NotBlank(message = "Confirm Password cannot be empty")
    private String confirmPassword;

    @Pattern(
            regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",
            message = "Invalid email address"
    )
    private String email;

    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Invalid phone number"
    )
    private String phoneNumber;
}