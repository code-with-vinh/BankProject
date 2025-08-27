package com.banking.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())   // tắt CSRF
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()   // cho phép tất cả request mà không cần đăng nhập
                )
                .httpBasic(basic -> basic.disable()); // tắt Basic Auth nếu dùng

        return http.build();
    }
}
