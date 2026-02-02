package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.User;
import com.epam.finaltask.model.VoucherStatus;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vouchers")
@AllArgsConstructor
public class VoucherRestController {

    private final VoucherService voucherService;
    private final UserRepository userRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAll() {
        List<VoucherDTO> vouchers = voucherService.findAll();
        ApiResponse<List<VoucherDTO>> response = new ApiResponse<>();
        response.setResults(vouchers);
        response.setStatusCode("OK");
        response.setStatusMessage("All vouchers retrieved successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> createVoucher(@Valid @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO createdVoucher = voucherService.create(voucherDTO);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(createdVoucher);
        response.setStatusCode("OK");
        response.setStatusMessage("Voucher is successfully created");

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/order/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VoucherDTO>> orderVoucher(@PathVariable String id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        VoucherDTO ordered = voucherService.order(id, user.getId().toString());

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(ordered);
        response.setStatusCode("OK");
        response.setStatusMessage("Voucher successfully ordered");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> updateVoucher(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {
        VoucherDTO updatedVoucher = voucherService.update(id, voucherDTO);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(updatedVoucher);
        response.setStatusCode("OK");
        response.setStatusMessage("Voucher is successfully updated");

        return ResponseEntity
                .ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVoucher(@PathVariable String id) {
        voucherService.delete(id);

        ApiResponse<Void> response = new ApiResponse<>();
        response.setStatusCode("OK");
        response.setStatusMessage(String.format("Voucher with Id %s has been deleted", id));

        return ResponseEntity
                .ok(response);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ApiResponse<VoucherDTO>> changeVoucherStatus(@PathVariable String id, @RequestBody VoucherDTO voucherDTO) {

        VoucherDTO updatedVoucher = voucherService.changeHotStatus(id, voucherDTO);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(updatedVoucher);
        response.setStatusCode("OK");
        response.setStatusMessage("Voucher status is successfully changed");

        return ResponseEntity
                .ok(response);
    }

    @GetMapping("/catalog")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> catalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,

            @RequestParam(required = false) TourType tourType,
            @RequestParam(required = false) TransferType transferType,
            @RequestParam(required = false) HotelType hotelType,

            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "isHot");

        Sort secondarySort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        sort = sort.and(secondarySort);

        Pageable pageable = PageRequest.of(page, size, sort);

        String username = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                username = auth.getName();
            }
        } catch (Exception ignored) {}

        Page<VoucherDTO> voucherPage =
                voucherService.findCatalogFiltered(
                        tourType,
                        transferType,
                        hotelType,
                        description,
                        minPrice,
                        maxPrice,
                        pageable,
                        username
                );

        Map<String, Object> response = new HashMap<>();
        response.put("vouchers", voucherPage.getContent());
        response.put("currentPage", voucherPage.getNumber());
        response.put("totalPages", voucherPage.getTotalPages());
        response.put("totalElements", voucherPage.getTotalElements());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> myVouchers(Authentication authentication) {
        String username = authentication.getName();

        List<VoucherDTO> vouchers = voucherService.findMyVouchers(username);

        ApiResponse<List<VoucherDTO>> response = new ApiResponse<>();
        response.setResults(vouchers);
        response.setStatusCode("OK");
        response.setStatusMessage("My vouchers retrieved successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VoucherDTO>> requestCancellation(@PathVariable String id,
                                                                       @RequestBody Map<String, String> body,
                                                                       Authentication authentication) {
        String username = authentication.getName();
        String reason = body != null ? body.get("reason") : null;

        VoucherDTO updated = voucherService.requestCancellation(id, username, reason);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(updated);
        response.setStatusCode("OK");
        response.setStatusMessage("Cancellation requested successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> decideCancellation(@PathVariable String id,
                                                                      @RequestBody Map<String, Object> body,
                                                                      Authentication authentication) {
        boolean approved = false;
        if (body != null && body.get("approved") instanceof Boolean) {
            approved = (Boolean) body.get("approved");
        }

        String adminUsername = authentication.getName();
        VoucherDTO updated = voucherService.decideCancellation(id, approved, adminUsername);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(updated);
        response.setStatusCode("OK");
        response.setStatusMessage(approved ? "Cancellation approved" : "Cancellation rejected");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/canceled")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findCanceled() {
        List<VoucherDTO> canceled = voucherService.findAll().stream()
                .filter(v -> v.getStatus() == VoucherStatus.CANCELED)
                .toList();

        ApiResponse<List<VoucherDTO>> response = new ApiResponse<>();
        response.setResults(canceled);
        response.setStatusCode("OK");
        response.setStatusMessage("Canceled vouchers retrieved successfully");

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reregister")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VoucherDTO>> reregisterVoucher(@PathVariable String id, Authentication authentication) {
        String adminUsername = authentication.getName();
        VoucherDTO updated = voucherService.reregisterVoucher(id, adminUsername);

        ApiResponse<VoucherDTO> response = new ApiResponse<>();
        response.setResults(updated);
        response.setStatusCode("OK");
        response.setStatusMessage("Voucher reregistered successfully");

        return ResponseEntity.ok(response);
    }

}