package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.TransactionException;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.exception.UsernameAlreadyExistsException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	public UserServiceImpl(UserRepository userRepository,
						   UserMapper userMapper,
						   PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDTO register(UserDTO userDTO) {
		if (userRepository.existsByUsername(userDTO.getUsername())) {
			throw new UsernameAlreadyExistsException("Username already exists");
		}

		User user = userMapper.toUser(userDTO);

		user.setId(UUID.randomUUID());
		user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		user.setRole(Role.CUSTOMER);
		user.setBalance(BigDecimal.valueOf(0));
		user.setActive(true);

		User savedUser = userRepository.save(user);

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO changeUserActive(UUID id, boolean active) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		user.setActive(active);
		userRepository.save(user);

		return mapToUserDTO(user);
	}

	@Override
	public UserDTO updateUser(String username, UserDTO userDTO) {
		User existingUser = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		Optional.ofNullable(userDTO.getPassword())
				.filter(password -> !password.isEmpty())
				.ifPresent(password -> existingUser.setPassword(passwordEncoder.encode(password)));

		Optional.ofNullable(userDTO.getPhoneNumber())
				.ifPresent(existingUser::setPhoneNumber);

		Optional.ofNullable(userDTO.getBalance())
				.ifPresent(existingUser::setBalance);

		User savedUser = userRepository.save(existingUser);

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserByUsername(String username) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return userMapper.toUserDTO(user);
	}

	@Override
	public UserDTO getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return userMapper.toUserDTO(user);
	}

	@Override
	public List<UserDTO> findAllUsers() {
		return userRepository.findAll()
				.stream()
				.map(this::mapToUserDTO)
				.toList();
	}


	public UserDTO deposit(String username, BigDecimal amount, String cardNumber, String expiry, String cvv) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		validateDeposit(amount, cardNumber, expiry, cvv);

		user.setBalance(user.getBalance().add(amount));
		userRepository.save(user);

		return mapToUserDTO(user);
	}

	public UserDTO withdraw(String username, BigDecimal amount, String cardNumber) {
		User user = userRepository.findUserByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		validateWithdraw(user, amount, cardNumber);

		user.setBalance(user.getBalance().subtract(amount));
		userRepository.save(user);

		return mapToUserDTO(user);
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
