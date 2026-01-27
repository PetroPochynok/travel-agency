package com.epam.finaltask.dto;

import java.math.BigDecimal;
import java.util.List;

import com.epam.finaltask.model.Voucher;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {

	private String id;

	private String username;

	private String password;

	private String email;

	private String firstName;

	private String lastName;

	private String role;

	private List<Voucher> vouchers;

	private String phoneNumber;

	private BigDecimal balance;

	private boolean active;

}

