package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import com.banking.Service.AccountService;
import com.banking.Service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepo;

    // Hiển thị form đăng ký
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("account", new Account());
        return "auth/register";
    }

    // Xử lý đăng ký
    @PostMapping("/register")
    public String register(@ModelAttribute("account") Account account, Model model) {
        try {
            authService.register(account);
            model.addAttribute("message", "Đăng ký thành công!");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }



    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String email,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        Account acc = authService.login(email, password);
        if (acc == null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "auth/login";
        }
        session.setAttribute("account", acc);
        return "home/homePage";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }

}
