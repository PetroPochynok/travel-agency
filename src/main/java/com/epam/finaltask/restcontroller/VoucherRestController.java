package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.exception.UserNotFoundException;
import com.epam.finaltask.model.HotelType;
import com.epam.finaltask.model.TourType;
import com.epam.finaltask.model.TransferType;
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
    public ResponseEntity<Map<String, Object>> catalog(@AuthenticationPrincipal UserDetails userDetails) {
        List<VoucherDTO> vouchers = voucherService.findCatalog();
        User user = userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow();

        Map<String, Object> response = new HashMap<>();
        response.put("vouchers", vouchers);
        response.put("balance", user.getBalance());

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

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> searchVouchers(
            @RequestParam(required = false) TourType tourType,
            @RequestParam(required = false) TransferType transferType,
            @RequestParam(required = false) HotelType hotelType,
            @RequestParam(required = false) Double price,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy) {

        List<VoucherDTO> vouchers;

        if (tourType != null) {
            vouchers = voucherService.findAllByTourType(tourType, page, size, sortBy);
        } else if (transferType != null) {
            vouchers = voucherService.findAllByTransferType(transferType, page, size, sortBy);
        } else if (hotelType != null) {
            vouchers = voucherService.findAllByHotelType(hotelType, page, size, sortBy);
        } else if (price != null) {
            vouchers = voucherService.findAllByPrice(price, page, size, sortBy);
        } else {
            vouchers = voucherService.findCatalog();
        }

        ApiResponse<List<VoucherDTO>> response = new ApiResponse<>();
        response.setResults(vouchers);
        response.setStatusCode("OK");
        response.setStatusMessage("Filtered vouchers retrieved successfully");

        return ResponseEntity.ok(response);
    }
}