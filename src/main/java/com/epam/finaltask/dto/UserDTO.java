package com.epam.finaltask.dto;

import java.math.BigDecimal;

import com.epam.finaltask.annotation.Sensitive;
import lombok.Data;

@Data
public class UserDTO {

	private String id;

	private String username;

	@Sensitive
	private String password;

	private String email;

	private String firstName;

	private String lastName;

	private String role;

	private String phoneNumber;

	private BigDecimal balance;

	private boolean active;

}

