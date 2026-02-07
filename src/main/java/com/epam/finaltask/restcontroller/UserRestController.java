package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UpdatePasswordRequest;
import com.epam.finaltask.dto.UpdateUserRequest;
import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.service.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final MessageSource messageSource;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO dto = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCurrentUser(@AuthenticationPrincipal UserDetails userDetails,
                                               @Valid @RequestBody UpdateUserRequest request,
                                               BindingResult bindingResult,
                                               Locale locale) {
        Map<String, String> errors = new HashMap<>();
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors()
                    .forEach(error -> errors.put(error.getField(), messageSource.getMessage(error, locale)));
        }
        if (request.getEmail() != null && userService.isEmailTaken(request.getEmail(), userDetails.getUsername())) {
            errors.put("email", messageSource.getMessage("user.email.exists", null, locale));
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }
        UserDTO payload = new UserDTO();
        payload.setFirstName(request.getFirstName());
        payload.setLastName(request.getLastName());
        payload.setEmail(request.getEmail());
        payload.setPhoneNumber(request.getPhoneNumber());
        UserDTO dto = userService.updateUser(userDetails.getUsername(), payload);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @Valid @RequestBody UpdatePasswordRequest request,
                                            BindingResult bindingResult,
                                            Locale locale) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors()
                    .forEach(error -> errors.put(error.getField(), messageSource.getMessage(error, locale)));
        }

        if (request.getNewPassword() != null
                && request.getConfirmPassword() != null
                && !request.getNewPassword().equals(request.getConfirmPassword())) {
            errors.put("confirmPassword", messageSource.getMessage("profile.password.mismatch", null, locale));
        }

        if (request.getCurrentPassword() != null
                && !userService.isCurrentPasswordValid(userDetails.getUsername(), request.getCurrentPassword())) {
            errors.put("currentPassword", messageSource.getMessage("profile.password.invalidCurrent", null, locale));
        }

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(errors);
        }

        userService.changePassword(userDetails.getUsername(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "profile.password.updated"));
    }



    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> findAllUsers() {
        List<UserDTO> users = userService.findAllUsers();

        ApiResponse<List<UserDTO>> response = new ApiResponse<>();
        response.setResults(users);
        response.setStatusCode("OK");
        response.setStatusMessage("All users retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> changeUserActive(@PathVariable String id,
                                                                 @RequestBody(required = false) Map<String, Object> body) {
        boolean active = body != null && body.get("active") instanceof Boolean ? (Boolean) body.get("active") : true;

        UserDTO dto = userService.changeUserActive(UUID.fromString(id), active);

        ApiResponse<UserDTO> response = new ApiResponse<>();
        response.setResults(dto);
        response.setStatusCode("OK");
        response.setStatusMessage("User active status updated");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserDTO>> deposit(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody Map<String, String> body) {
        ApiResponse<UserDTO> response = new ApiResponse<>();
        try {
            String username = userDetails.getUsername();
            BigDecimal amount = new BigDecimal(body.get("amount"));
            String cardNumber = body.get("cardNumber");
            String expiry = body.get("expiry");
            String cvv = body.get("cvv");

            UserDTO dto = userService.deposit(username, amount, cardNumber, expiry, cvv);

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
    public ResponseEntity<ApiResponse<UserDTO>> withdraw(@AuthenticationPrincipal UserDetails userDetails,
                                                         @RequestBody Map<String, String> body) {
        ApiResponse<UserDTO> response = new ApiResponse<>();
        try {
            String username = userDetails.getUsername();
            BigDecimal amount = new BigDecimal(body.get("amount"));
            String cardNumber = body.get("cardNumber");

            UserDTO dto = userService.withdraw(username, amount, cardNumber);

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

}
