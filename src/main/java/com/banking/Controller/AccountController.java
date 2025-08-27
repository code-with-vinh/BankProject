package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import com.banking.Service.AccountService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepo;

    @GetMapping("/profile")
    private String showProfile(HttpSession session, Model model){
        Account acc = (Account) session.getAttribute("account");

        if (acc == null) {
            return "redirect:/auth/login";
        }
        model.addAttribute("account", acc);
        return "customer/profile";
    }
}
