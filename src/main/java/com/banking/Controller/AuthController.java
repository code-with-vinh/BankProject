package com.banking.Controller;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import com.banking.Service.AccountService;
import com.banking.Service.AuthService;
import com.banking.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Authentication Controller
 * 
 * Handles user authentication operations including registration, login, and logout.
 * Manages JWT token creation and cookie handling for session management.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
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

    /**
     * Display the user registration form
     * 
     * @param model Spring MVC model to pass data to the view
     * @return The registration form view name
     */
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("account", new Account());
        return "auth/register";
    }

    /**
     * Process user registration
     * 
     * @param account The account information from the registration form
     * @param model Spring MVC model for passing messages to the view
     * @return Redirect to login page on success, or back to registration form on error
     */
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



    /**
     * Display the user login form
     * 
     * @return The login form view name
     */
    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    /**
     * Process user login authentication
     * 
     * @param email User's email address
     * @param password User's password
     * @param response HTTP response for setting cookies
     * @param attributes Redirect attributes for passing messages
     * @return Redirect to appropriate dashboard based on user role
     */
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

        // Create JWT token for session management
        String token = jwtUtil.generateToken(acc.getEmail(), acc.getRole());
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        String redirectUrl = "Customer".equalsIgnoreCase(acc.getRole()) ? "/bank" : "/admin/dashboard";
        return new RedirectView(redirectUrl);
    }

    /**
     * Process user logout
     * 
     * @param response HTTP response for clearing cookies
     * @return Redirect to home page
     */
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
