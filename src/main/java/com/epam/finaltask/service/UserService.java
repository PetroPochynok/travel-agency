package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;

public interface UserService {
    UserDTO updateUser(String username, UserDTO userDTO);
    UserDTO getUserById(UUID id);
    List<UserDTO> findAllUsers();
    UserDTO getUserByUsername(String username);
    UserDTO changeUserActive(UUID id, boolean active);
    UserDTO deposit(String username, BigDecimal amount, String cardNumber, String expiry, String cvv);
    UserDTO withdraw(String username, BigDecimal amount, String cardNumber);
    void changePassword(String username, String currentPassword, String newPassword);
    boolean isCurrentPasswordValid(String username, String currentPassword);
    boolean isEmailTaken(String email, String username);
}
