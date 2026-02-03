package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRestController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findUserByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDTO dto = new UserDTO();
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setBalance(user.getBalance());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<UserDTO>>> findAllUsers() {
        List<UserDTO> users = userRepository.findAll()
                .stream()
                .map(this::mapToUserDTO)
                .toList();

        ApiResponse<List<UserDTO>> response = new ApiResponse<>();
        response.setResults(users);
        response.setStatusCode("OK");
        response.setStatusMessage("All users retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> changeUserActive(@PathVariable String id, @RequestBody(required = false) Map<String, Object> body) {
        boolean active = true;
        if (body != null && body.get("active") instanceof Boolean) {
            active = (Boolean) body.get("active");
        }

        User user = userRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setActive(active);
        userRepository.save(user);

        UserDTO dto = mapToUserDTO(user);

        ApiResponse<UserDTO> response = new ApiResponse<>();
        response.setResults(dto);
        response.setStatusCode("OK");
        response.setStatusMessage("User active status updated");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> deposit(@AuthenticationPrincipal UserDetails userDetails,
                                                         @RequestBody Map<String, String> body) {
        String username = userDetails.getUsername();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String amountStr = body.get("amount");
        String cardNumber = body.get("cardNumber");
        String expiry = body.get("expiry");
        String cvv = body.get("cvv");

        ApiResponse<UserDTO> response = new ApiResponse<>();

        try {
            if (amountStr == null) throw new IllegalArgumentException("Invalid amount");
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

            if (cardNumber == null || !cardNumber.matches("\\d{16}")) throw new IllegalArgumentException("Invalid card number");
            if (cvv == null || !cvv.matches("\\d{3}")) throw new IllegalArgumentException("Invalid CVV");
            if (expiry == null || !expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) throw new IllegalArgumentException("Invalid expiry format");

            String[] parts = expiry.split("/");
            int mm = Integer.parseInt(parts[0]);
            int yy = Integer.parseInt(parts[1]);
            int year = 2000 + yy;
            YearMonth cardYm = YearMonth.of(year, mm);
            YearMonth nowYm = YearMonth.from(LocalDate.now());
            if (cardYm.isBefore(nowYm)) throw new IllegalArgumentException("Card expired");

            user.setBalance(user.getBalance().add(amount));
            userRepository.save(user);

            UserDTO dto = mapToUserDTO(user);
            response.setResults(dto);
            response.setStatusCode("OK");
            response.setStatusMessage("Deposit successful");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.setStatusCode("ERROR");
            response.setStatusMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.setStatusCode("ERROR");
            response.setStatusMessage("Unexpected error");
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<UserDTO>> withdraw(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestBody Map<String, String> body) {
        String username = userDetails.getUsername();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String amountStr = body.get("amount");
        String cardNumber = body.get("cardNumber");

        ApiResponse<UserDTO> response = new ApiResponse<>();

        try {
            if (amountStr == null) throw new IllegalArgumentException("Invalid amount");
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Amount must be positive");

            if (cardNumber == null || !cardNumber.matches("\\d{16}")) throw new IllegalArgumentException("Invalid card number");

            if (user.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient balance");

            user.setBalance(user.getBalance().subtract(amount));
            userRepository.save(user);

            UserDTO dto = mapToUserDTO(user);
            response.setResults(dto);
            response.setStatusCode("OK");
            response.setStatusMessage("Withdraw successful");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            response.setStatusCode("ERROR");
            response.setStatusMessage(ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception ex) {
            response.setStatusCode("ERROR");
            response.setStatusMessage("Unexpected error");
            return ResponseEntity.status(500).body(response);
        }
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
