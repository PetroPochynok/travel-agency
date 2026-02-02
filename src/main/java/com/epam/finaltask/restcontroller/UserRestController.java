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
import org.springframework.web.bind.annotation.*;

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
