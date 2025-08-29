package com.banking.Security;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.Cookie;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = null;
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            token = bearerToken.substring(7);
        }
        if (token == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("JWT".equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
        }
        if (token != null) {
            try {
                String email = jwtUtil.getEmailFromToken(token);
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Optional<Account> accountOpt = accountRepository.findByEmail(email);
                    if (accountOpt.isPresent()) {
                        Account account = accountOpt.get();
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + account.getRole());
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                List.of(authority)
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception ex) {
                // Invalid token: leave SecurityContext unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}

