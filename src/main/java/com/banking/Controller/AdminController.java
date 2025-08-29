package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Entity.Card;
import com.banking.Entity.Transaction;
import com.banking.Service.AccountService;
import com.banking.Service.AdminService;
import com.banking.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    // Kiểm tra quyền admin
    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_Admin"));
    }

    // Dashboard admin
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        long totalAccounts = adminService.getTotalAccounts();
        long totalCards = adminService.getTotalCards();
        long totalTransactions = adminService.getTotalTransactions();
        
        model.addAttribute("totalAccounts", totalAccounts);
        model.addAttribute("totalCards", totalCards);
        model.addAttribute("totalTransactions", totalTransactions);
        
        return "admin/dashboard";
    }

    // Danh sách tài khoản
    @GetMapping("/accounts")
    public String listAccounts(Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        List<Account> accounts = adminService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "admin/accounts";
    }

    // Danh sách thẻ
    @GetMapping("/cards")
    public String listCards(Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        List<Card> cards = adminService.getAllCards();
        model.addAttribute("cards", cards);
        return "admin/cards";
    }

    // Danh sách giao dịch
    @GetMapping("/transactions")
    public String listTransactions(Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        List<Transaction> transactions = adminService.getAllTransactions();
        model.addAttribute("transactions", transactions);
        model.addAttribute("ZERO", BigDecimal.ZERO);
        return "admin/transactions";
    }

    // Form tạo user mới
    @GetMapping("/create-user")
    public String createUserForm(Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        return "admin/createUser";
    }

    // Xử lý tạo user mới
    @PostMapping("/create-user")
    public String createUser(@RequestParam("customerName") String customerName,
                           @RequestParam("email") String email,
                           @RequestParam("password") String password,
                           @RequestParam("role") String role,
                           @RequestParam("phoneNumber") String phoneNumber,
                           Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.createUser(customerName, email, password, role, phoneNumber);
            model.addAttribute("success", "Tạo tài khoản thành công!");
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "admin/createUser";
    }

    // Xóa tài khoản
    @PostMapping("/delete-account/{id}")
    public String deleteAccount(@PathVariable("id") Long accountId, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.deleteAccount(accountId);
            redirectAttributes.addFlashAttribute("success", "Xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    // Cập nhật role
    @PostMapping("/update-role/{id}")
    public String updateRole(@PathVariable("id") Long accountId,
                           @RequestParam("role") String role,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.updateUserRole(accountId, role);
            redirectAttributes.addFlashAttribute("success", "Cập nhật role thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    // Cập nhật level
    @PostMapping("/update-level/{id}")
    public String updateLevel(@PathVariable("id") Long accountId,
                           @RequestParam("level") String level,
                           RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.updateUserLevel(accountId, level);
            redirectAttributes.addFlashAttribute("success", "Cập nhật level thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/accounts";
    }

    // Nạp tiền vào tài khoản debit
    @PostMapping("/deposit/{cardId}")
    public String depositToCard(@PathVariable("cardId") Long cardId,
                              @RequestParam("amount") String amount,
                              RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.depositToCard(cardId, amount);
            redirectAttributes.addFlashAttribute("success", "Nạp tiền thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/cards";
    }

    // Cập nhật trạng thái thẻ
    @PostMapping("/update-card-status/{id}")
    public String updateCardStatus(@PathVariable("id") Long cardId,
                                 @RequestParam("status") String status,
                                 RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.updateCardStatus(cardId, status);
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/cards";
    }

    // Xóa thẻ
    @PostMapping("/delete-card/{id}")
    public String deleteCard(@PathVariable("id") Long cardId, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        try {
            adminService.deleteCard(cardId);
            redirectAttributes.addFlashAttribute("success", "Xóa thẻ thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/cards";
    }

    // Xem chi tiết tài khoản
    @GetMapping("/account/{id}")
    public String viewAccount(@PathVariable("id") Long accountId, Model model) {
        if (!isAdmin()) {
            return "redirect:/auth/login";
        }
        
        Account account = adminService.getAccountById(accountId);
        if (account == null) {
            return "redirect:/admin/accounts";
        }
        
        List<Card> cards = adminService.getCardsByAccount(accountId);
        List<Transaction> transactions = adminService.getTransactionsByAccount(accountId);
        
        model.addAttribute("account", account);
        model.addAttribute("cards", cards);
        model.addAttribute("transactions", transactions);
        
        return "admin/accountDetail";
    }

}
