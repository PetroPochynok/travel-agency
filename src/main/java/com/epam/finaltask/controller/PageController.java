package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final VoucherService voucherService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/catalog")
    public String catalogPage() {
        return "catalog";
    }

    @GetMapping("/catalog/my")
    public String myVouchersPage() {
        return "my-vouchers";
    }

    @GetMapping("/admin/vouchers/{id}/edit")
    public String editVoucherPage(@PathVariable String id, Model model) {
        VoucherDTO voucher = voucherService.findById(id);
        model.addAttribute("voucher", voucher);
        return "voucher-edit";
    }

    @GetMapping("/admin/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminRequestsPage() {
        return "admin-requests";
    }
}