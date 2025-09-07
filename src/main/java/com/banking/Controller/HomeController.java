package com.banking.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import com.banking.Repository.AccountRepository;
import com.banking.Entity.Account;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Home Controller
 * 
 * Handles basic navigation and home page functionality.
 * Provides access to customer home page and admin dashboard.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@Controller
@RequestMapping("/bank")
public class HomeController {

    /**
     * Display the customer home page
     * 
     * @return The customer home page view name
     */
    @GetMapping()
    public String hello(){
        return "home/homePage";
    }

    /**
     * Display the admin dashboard
     * 
     * @return The admin dashboard view name
     */
    @GetMapping("/admin")
    public String admin(){
        return "home/adminDashboard";
    }

    @Autowired
    private AccountRepository accountRepository;

    /**
     * Change the account level for a specific user
     * 
     * @param email The email of the account to update
     * @param level The new level to set (SILVER, GOLD, PLATINUM)
     * @param model Spring MVC model for passing data to the view
     * @return Redirect back to admin dashboard
     */
    @PostMapping("/admin/change-level")
    public String changeLevel(@RequestParam("email") String email,
                              @RequestParam("level") String level,
                              Model model){
        Optional<Account> accOpt = accountRepository.findByEmail(email);
        accOpt.ifPresent(acc -> {
            acc.setLevel(level.toUpperCase());
            accountRepository.save(acc);
        });
        return "redirect:/bank/admin";
    }
}
