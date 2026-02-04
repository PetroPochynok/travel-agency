package com.epam.finaltask.service;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.TransactionException;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.exception.UsernameAlreadyExistsException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @Order(1)
    void register_success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("customer");
        userDTO.setPassword("12345");

        User user = new User();
        user.setUsername("customer");
        UUID userId = UUID.randomUUID();

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userMapper.toUser(userDTO)).thenReturn(user);
        when(passwordEncoder.encode("12345")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(userId);
            return savedUser;
        });
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.register(userDTO);

        assertNotNull(result);
        verify(userRepository).save(any(User.class));

        assertNotNull(user.getId());
        assertEquals(Role.CUSTOMER, user.getRole());
        assertEquals(BigDecimal.ZERO, user.getBalance());
        assertTrue(user.isActive());
        assertEquals("ENCODED", user.getPassword());
    }

    @Test
    @Order(2)
    void register_usernameAlreadyExists_throwException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("customer");

        when(userRepository.existsByUsername("customer")).thenReturn(true);

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> userService.register(userDTO)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @Order(3)
    void register_passwordIsEncoded() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("customer");
        userDTO.setPassword("12345");

        User user = new User();

        when(userRepository.existsByUsername("customer")).thenReturn(false);
        when(userMapper.toUser(userDTO)).thenReturn(user);
        when(passwordEncoder.encode("12345")).thenReturn("ENCODED");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(any(User.class))).thenReturn(userDTO);

        userService.register(userDTO);

        verify(passwordEncoder).encode("12345");
        assertEquals("ENCODED", user.getPassword());
    }

    @Test
    @Order(4)
    void changeUserActive_success() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserDTO result = userService.changeUserActive(userId, true);

        assertNotNull(result);
        assertTrue(user.isActive());

        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
        verifyNoInteractions(userMapper);
    }

    @Test
    @Order(5)
    void changeUserActive_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.changeUserActive(userId, true)
        );

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @Order(6)
    void updateUser_success() {
        String username = "customer";

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword("newPass");
        userDTO.setPhoneNumber("1234567890");
        userDTO.setBalance(BigDecimal.valueOf(100));

        User existingUser = new User();
        existingUser.setUsername(username);
        existingUser.setPassword("oldPass");
        existingUser.setPhoneNumber("0000000000");
        existingUser.setBalance(BigDecimal.ZERO);

        User savedUser = new User();
        savedUser.setUsername(username);
        savedUser.setPassword("encodedPass");
        savedUser.setPhoneNumber("1234567890");
        savedUser.setBalance(BigDecimal.valueOf(100));

        UserDTO returnedDTO = new UserDTO();
        returnedDTO.setPassword("encodedPass");
        returnedDTO.setPhoneNumber("1234567890");
        returnedDTO.setBalance(BigDecimal.valueOf(100));

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedPass");
        when(userRepository.save(existingUser)).thenReturn(savedUser);
        when(userMapper.toUserDTO(savedUser)).thenReturn(returnedDTO);

        UserDTO result = userService.updateUser(username, userDTO);

        assertNotNull(result);
        assertEquals("encodedPass", result.getPassword());
        assertEquals("1234567890", result.getPhoneNumber());
        assertEquals(BigDecimal.valueOf(100), result.getBalance());

        verify(userRepository).findUserByUsername(username);
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(existingUser);
        verify(userMapper).toUserDTO(savedUser);
    }

    @Test
    @Order(7)
    void updateUser_userNotFound_throwException() {
        String username = "nonexistent";
        UserDTO userDTO = new UserDTO();

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(username, userDTO)
        );

        verify(userRepository).findUserByUsername(username);
        verifyNoInteractions(passwordEncoder, userMapper);
    }


    @Test
    @Order(8)
    void updateUser_partialUpdate_ignoresNulls() {
        String username = "customer";

        UserDTO userDTO = new UserDTO();
        userDTO.setPassword(null);
        userDTO.setPhoneNumber("5555555");

        User existingUser = new User();
        existingUser.setUsername(username);
        existingUser.setPassword("oldPass");
        existingUser.setPhoneNumber("0000000");

        User savedUser = new User();
        savedUser.setUsername(username);
        savedUser.setPassword("oldPass");
        savedUser.setPhoneNumber("5555555");

        UserDTO returnedDTO = new UserDTO();
        returnedDTO.setPassword("oldPass");
        returnedDTO.setPhoneNumber("5555555");

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(savedUser);
        when(userMapper.toUserDTO(savedUser)).thenReturn(returnedDTO);

        UserDTO result = userService.updateUser(username, userDTO);

        assertEquals("oldPass", result.getPassword());
        assertEquals("5555555", result.getPhoneNumber());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @Order(9)
    void getUserByUsername_success() {
        String username = "customer";

        User user = new User();
        user.setUsername(username);

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserByUsername(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());

        verify(userRepository).findUserByUsername(username);
        verify(userMapper).toUserDTO(user);
    }

    @Test
    @Order(10)
    void getUserByUsername_userNotFound_throwException() {
        String username = "nonexistent";

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserByUsername(username)
        );

        verify(userRepository).findUserByUsername(username);

        verifyNoInteractions(userMapper);
    }

    @Test
    @Order(11)
    void getUserById_success() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId.toString());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId.toString(), result.getId());

        verify(userRepository).findById(userId);
        verify(userMapper).toUserDTO(user);
    }

    @Test
    @Order(12)
    void getUserById_userNotFound_throwException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(userId)
        );

        verify(userRepository).findById(userId);

        verifyNoInteractions(userMapper);
    }

    @Test
    @Order(13)
    void findAllUsers_success() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        User user2 = new User();
        user2.setId(UUID.randomUUID());

        List<User> users = List.of(user1, user2);

        UserDTO dto1 = new UserDTO();
        dto1.setId(user1.getId().toString());
        UserDTO dto2 = new UserDTO();
        dto2.setId(user2.getId().toString());

        when(userRepository.findAll()).thenReturn(users);

        List<UserDTO> result = userService.findAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(user1.getId().toString())));
        assertTrue(result.stream().anyMatch(u -> u.getId().equals(user2.getId().toString())));

        verify(userRepository).findAll();
    }

    @Test
    @Order(14)
    void findAllUsers_emptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDTO> result = userService.findAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

    @Test
    @Order(15)
    void deposit_success() {
        String username = "customer";
        BigDecimal depositAmount = BigDecimal.valueOf(300);
        String cardNumber = "1234567890123456";
        String cardExpiry = "12/27";
        String cvv = "123";

        User user = new User();
        user.setUsername(username);
        user.setBalance(BigDecimal.ZERO);

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserDTO result = userService.deposit(username, depositAmount, cardNumber, cardExpiry, cvv);

        assertNotNull(result);
        assertEquals(depositAmount, result.getBalance());

        verify(userRepository).findUserByUsername(username);
        verify(userRepository).save(user);
    }

    @Test
    @Order(16)
    void deposit_userNotFound_throwsException() {
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userService.deposit("unknown", BigDecimal.valueOf(100), "1234567890123456", "12/27", "123")
        );
    }

    @Test
    @Order(17)
    void deposit_negativeAmount_throwsTransactionException() {
        when(userRepository.findUserByUsername("customer")).thenReturn(Optional.of(new User()));

        assertThrows(TransactionException.class, () ->
                userService.deposit("customer", BigDecimal.valueOf(-10), "1234567890123456", "12/27", "123")
        );
    }

    @Test
    @Order(18)
    void withdraw_success() {
        String username = "customer";
        BigDecimal withdrawAmount = BigDecimal.valueOf(200);
        String cardNumber = "1234567812345678";

        User user = new User();
        user.setUsername(username);
        user.setBalance(BigDecimal.valueOf(500));

        when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.withdraw(username, withdrawAmount, cardNumber);

        assertNotNull(result, "Returned UserDTO should not be null");
        assertEquals(username, result.getUsername(), "Username should match");
        assertEquals(BigDecimal.valueOf(300), user.getBalance(), "User balance should be reduced by withdraw amount");
    }

    @Test
    @Order(19)
    void withdraw_userNotFound_throwsException() {
        when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                userService.withdraw("unknown", BigDecimal.valueOf(100), "1234567812345678")
        );
    }

    @Test
    @Order(20)
    void withdraw_negativeAmount_throwsTransactionException() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(500));
        when(userRepository.findUserByUsername("customer")).thenReturn(Optional.of(user));

        assertThrows(TransactionException.class, () ->
                userService.withdraw("customer", BigDecimal.valueOf(-50), "1234567812345678")
        );

        assertThrows(TransactionException.class, () ->
                userService.withdraw("customer", BigDecimal.ZERO, "1234567812345678")
        );
    }

    @Test
    @Order(21)
    void withdraw_invalidCard_throwsTransactionException() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(500));
        when(userRepository.findUserByUsername("customer")).thenReturn(Optional.of(user));

        assertThrows(TransactionException.class, () ->
                userService.withdraw("customer", BigDecimal.valueOf(100), "1234")
        );

        assertThrows(TransactionException.class, () ->
                userService.withdraw("customer", BigDecimal.valueOf(100), null)
        );
    }

    @Test
    @Order(22)
    void withdraw_insufficientBalance_throwsTransactionException() {
        User user = new User();
        user.setBalance(BigDecimal.valueOf(100));
        when(userRepository.findUserByUsername("customer")).thenReturn(Optional.of(user));

        assertThrows(TransactionException.class, () ->
                userService.withdraw("customer", BigDecimal.valueOf(200), "1234567812345678")
        );
    }

}
