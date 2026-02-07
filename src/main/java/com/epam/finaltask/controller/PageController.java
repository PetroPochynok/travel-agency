package com.epam.finaltask.controller;

import com.epam.finaltask.dto.VoucherDTO;
import com.epam.finaltask.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("isAuthenticated()")
    public String catalogPage() {
        return "catalog";
    }

    @GetMapping("/catalog/my")
    @PreAuthorize("isAuthenticated()")
    public String myVouchersPage() {
        return "my-vouchers";
    }

    @GetMapping("/admin/vouchers/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
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

    @GetMapping("/admin/canceled")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminCanceledPage() {
        return "admin-canceled";
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminUsersPage() {
        return "admin-users";
    }

    @GetMapping("/admin/create-voucher")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminCreateVoucherPage() {
        return "admin-create-voucher";
    }

    @GetMapping("/deposit")
    @PreAuthorize("isAuthenticated()")
    public String depositPage(){
        return "deposit";
    }

    @GetMapping("/withdraw")
    @PreAuthorize("isAuthenticated()")
    public String withdrawPage(){
        return "withdraw";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profilePage() {
        return "profile";
    }
}