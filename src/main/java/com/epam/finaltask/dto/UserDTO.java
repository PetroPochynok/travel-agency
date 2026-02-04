package com.epam.finaltask.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class UserDTO {

	private String id;

	private String username;

	private String password;

	private String email;

	private String firstName;

	private String lastName;

	private String role;

	private String phoneNumber;

	private BigDecimal balance;

	private boolean active;

}

