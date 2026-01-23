package com.epam.finaltask.dto;

import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

	private String id;

	@NotBlank(message = "Username cannot be empty")
	private String username;

	@NotBlank(message = "Password cannot be empty")
	@Size(min = 6, message = "Password must be at least 6 characters")
	private String password;

	@NotBlank(message = "Role is required")
	private String role;


	private List<Voucher> vouchers;

	@Pattern(
			regexp = "^\\+?[0-9]{9,15}$",
			message = "Invalid phone number"
	)
	private String phoneNumber;

	@PositiveOrZero(message = "Balance must be zero or positive")
	private Double balance;

	private boolean active;

}
