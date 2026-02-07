package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.EmailAlreadyExistsException;
import com.epam.finaltask.exception.TransactionException;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	@Override
	@Transactional
	public UserDTO changeUserActive(UUID id, boolean active) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setActive(active);
		userRepository.save(user);

		return mapToUserDTO(user);
	}

	@Override
	@Transactional
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User existingUser = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		Optional.ofNullable(userDTO.getFirstName())
				.ifPresent(existingUser::setFirstName);

		Optional.ofNullable(userDTO.getLastName())
				.ifPresent(existingUser::setLastName);

		Optional.ofNullable(userDTO.getEmail())
				.ifPresent(email -> {
					if (!email.equalsIgnoreCase(existingUser.getEmail()) && userRepository.existsByEmail(email)) {
						throw new EmailAlreadyExistsException("user.email.exists");
					}
					existingUser.setEmail(email);
				});

		Optional.ofNullable(userDTO.getPassword())
				.filter(password -> !password.isEmpty())
				.ifPresent(password -> existingUser.setPassword(passwordEncoder.encode(password)));

		Optional.ofNullable(userDTO.getPhoneNumber())
				.ifPresent(existingUser::setPhoneNumber);

		User savedUser = userRepository.save(existingUser);

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return userMapper.toUserDTO(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return userMapper.toUserDTO(user);
	}

	@Override
	@Transactional(readOnly = true)
	public List<UserDTO> findAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(this::mapToUserDTO)
				.toList();
	}

	@Override
	@Transactional
	public UserDTO deposit(String username, BigDecimal amount, String cardNumber, String expiry, String cvv) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		validateDeposit(amount, cardNumber, expiry, cvv);

		user.setBalance(user.getBalance().add(amount));
		userRepository.save(user);

		return mapToUserDTO(user);
	}

	@Override
	@Transactional
	public UserDTO withdraw(String username, BigDecimal amount, String cardNumber) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		validateWithdraw(user, amount, cardNumber);

		user.setBalance(user.getBalance().subtract(amount));
		userRepository.save(user);

		return mapToUserDTO(user);
	}

	@Override
	@Transactional
	public void changePassword(String username, String currentPassword, String newPassword) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
			throw new IllegalArgumentException("profile.password.invalidCurrent");
		}

		user.setPassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isCurrentPasswordValid(String username, String currentPassword) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return passwordEncoder.matches(currentPassword, user.getPassword());
	}

	private void validateDeposit(BigDecimal amount, String cardNumber, String expiry, String cvv) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
			throw new TransactionException("Amount must be positive");

		if (cardNumber == null || !cardNumber.matches("\\d{16}"))
			throw new TransactionException("Invalid card number");

		if (cvv == null || !cvv.matches("\\d{3}"))
			throw new TransactionException("Invalid CVV");

		if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$"))
			throw new TransactionException("Invalid expiry format");

		String[] parts = expiry.split("/");
		int mm = Integer.parseInt(parts[0]);
		int yy = Integer.parseInt(parts[1]);
		YearMonth cardYm = YearMonth.of(2000 + yy, mm);
		YearMonth nowYm = YearMonth.from(LocalDate.now());
		if (cardYm.isBefore(nowYm)) throw new TransactionException("Card expired");
	}

	private void validateWithdraw(User user, BigDecimal amount, String cardNumber) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
			throw new TransactionException("balance.invalidAmount");

		if (cardNumber == null || !cardNumber.matches("\\d{16}"))
			throw new TransactionException("balance.invalidCard");

		if (user.getBalance().compareTo(amount) < 0)
			throw new TransactionException("balance.insufficient");
	}

	@Override
	@Transactional(readOnly = true)
	public boolean isEmailTaken(String email, String username) {
		if (email == null || email.isBlank()) {
			return false;
		}
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		if (email.equalsIgnoreCase(user.getEmail())) {
			return false;
		}
		return userRepository.existsByEmail(email);
	}

	private UserDTO mapToUserDTO(User u) {
		UserDTO dto = new UserDTO();
		dto.setId(u.getId() != null ? u.getId().toString() : null);
		dto.setUsername(u.getUsername());
		dto.setFirstName(u.getFirstName());
		dto.setLastName(u.getLastName());
		dto.setEmail(u.getEmail());
		dto.setPhoneNumber(u.getPhoneNumber());
		dto.setBalance(u.getBalance());
		dto.setActive(u.isActive());
		if (u.getRole() != null) dto.setRole(u.getRole().name());
		return dto;
	}


}
