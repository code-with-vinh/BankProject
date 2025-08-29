package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import com.banking.Service.AccountService;
import com.banking.Service.AuthService;
import com.banking.Security.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Map;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private JwtUtil jwtUtil;

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
    public RedirectView doLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpServletResponse response,
                                RedirectAttributes attributes) {
        Account acc = authService.login(email, password);
        if (acc == null) {
            attributes.addFlashAttribute("error", "Email hoặc mật khẩu không đúng");
            return new RedirectView("/auth/login");
        }

        // Tạo JWT
        String token = jwtUtil.generateToken(acc.getEmail(), acc.getRole());
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        String redirectUrl = "Customer".equalsIgnoreCase(acc.getRole()) ? "/bank" : "/admin/dashboard";
        return new RedirectView(redirectUrl);
    }


    @GetMapping("/logout")
    @ResponseBody
    public RedirectView logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("JWT", "");
        jwtCookie.setMaxAge(0);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);
        return new RedirectView("/bank");
    }

}
