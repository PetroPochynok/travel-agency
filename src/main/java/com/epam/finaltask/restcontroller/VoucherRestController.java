package com.epam.finaltask.restcontroller;

import com.epam.finaltask.dto.ApiResponse;
import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
public class VoucherRestController {

    private final VoucherService voucherService;

    public VoucherRestController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAll() {
        List<VoucherDTO> vouchers = voucherService.findAll();
        return ResponseEntity
                .ok(new ApiResponse<>(vouchers));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<VoucherDTO>>> findAllByUserId(@PathVariable String userId) {
        List<VoucherDTO> vouchers = voucherService.findAllByUserId(userId);
        return ResponseEntity
                .ok(new ApiResponse<>(vouchers));
    }

    @PostMapping
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

}