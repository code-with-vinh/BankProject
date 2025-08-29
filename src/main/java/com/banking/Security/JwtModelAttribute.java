package com.banking.Security;

import com.banking.Entity.Account;
import com.banking.Service.AccountService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class JwtModelAttribute {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountService accountService;

    @ModelAttribute("currentAccount")
    public Account addAccountToModel(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JWT".equals(c.getName())) {
                    String token = c.getValue();
                    try {
                        String email = jwtUtil.getEmailFromToken(token);
                        return accountService.findByEmail(email); // trả về Account
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }
}
