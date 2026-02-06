package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.UserDTO;
import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserRestController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        UserDTO dto = userService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(dto);
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
