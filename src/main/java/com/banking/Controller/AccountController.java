package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Service.AccountService;
import com.banking.Entity.Card;
import com.banking.Entity.Balance;
import com.banking.Service.TransactionService;
import com.sun.jdi.request.DuplicateRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import com.banking.Security.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();


        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("")) {
            return "redirect:/auth/login";
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("account", acc);
        model.addAttribute("cards", accountService.findCardsByAccount(acc));
        model.addAttribute("balance", accountService.findBalanceByAccount(acc));
        return "customer/profile";
    }
    @GetMapping("/create-card")
    public String createCard(Model model){
        return "customer/createCard";
    }

    @PostMapping("/create-card")
    public String handleCreateCard(@RequestParam("cardType") String cardType,
                                   @RequestParam("status") String status,
                                   @RequestParam("expiryDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate expiryDate,
                                   Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }
        accountService.createCard(acc, cardType, expiryDate, status);
        return "redirect:/account/profile";
    }

    @PostMapping("/delete-card/{id}")
    public String deleteCard(@PathVariable("id") Long cardId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("")) {
            return "redirect:/auth/login";
        }
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }
        accountService.deleteCardIfOwned(acc, cardId);
        return "redirect:/account/profile";
    }

    @PostMapping("/update")
    public String updateAccount(@RequestParam(value = "email", required = false) String newEmail,
                                @RequestParam(value = "phone", required = false) String newPhone,
                                Model model,
                                HttpServletResponse response) {

        try {
            accountService.updateAccountInfo(newEmail, newPhone, response);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", "Email hoặc SĐT đã tồn tại");
            return "account/profile"; // load lại profile kèm lỗi
        } catch (UsernameNotFoundException e) {
            return "redirect:/auth/login";
        }

        return "redirect:/account/profile";
    }


    @PostMapping("/delete")
    public String deleteAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return "redirect:/auth/login";
        }
        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }
        boolean deleted = accountService.deleteAccountIfNoCardsAndZeroBalance(acc);
        if (!deleted) {
            return "redirect:/account/profile";
        }
        return "redirect:/auth/logout";
    }

    @GetMapping("/transfer/{cardSend}")
    public String transferForm(@PathVariable("cardSend") String cardSend, Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }

        model.addAttribute("cards", accountService.findCardsByAccount(acc));
        model.addAttribute("cardSend", cardSend); // truyền cardSend xuống form Thymeleaf
        return "customer/transfer";
    }

    @PostMapping("/transfer/{cardSend}")
    public String transferMoney(@PathVariable String cardSend,
                                @RequestParam String cardReceipt,
                                @RequestParam BigDecimal amount,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            String email;
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                email = userDetails.getUsername();
            } else {
                email = principal.toString();
            }
            Account acc = accountService.findByEmail(email);
            if (acc == null) {
                return "redirect:/auth/login";
            }

            transactionService.transfer(acc,cardSend, cardReceipt, amount);
            redirectAttributes.addFlashAttribute("success", "Chuyển khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        model.addAttribute("cardSend", cardSend);
        return "redirect:/account/transfer/" + cardSend;
    }


}
