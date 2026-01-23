package com.epam.finaltask.service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.exception.UsernameAlreadyExistsException;
import com.epam.finaltask.mapper.UserMapper;
import com.epam.finaltask.model.Role;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
		user.setRole(Role.CUSTOMER); 	// set default Role
		user.setBalance(BigDecimal.valueOf(0));
		user.setActive(true);

		User savedUser = userRepository.save(user);

		return userMapper.toUserDTO(savedUser);
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
				.ifPresent(b -> existingUser.setBalance(BigDecimal.valueOf(b)));

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
	public UserDTO changeAccountStatus(UserDTO userDTO) {
		User userFromDTO = userMapper.toUser(userDTO);

		userFromDTO.setId(UUID.fromString(userDTO.getId()));

		User existingUser = userRepository.findById(userFromDTO.getId())
				.orElseThrow(() -> new UserNotFoundException("User not found"));

		existingUser.setActive(userDTO.isActive());

		User savedUser = userRepository.save(existingUser);

		return userMapper.toUserDTO(savedUser);
	}

	@Override
	public UserDTO getUserById(UUID id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found"));
		return userMapper.toUserDTO(user);
	}

}
