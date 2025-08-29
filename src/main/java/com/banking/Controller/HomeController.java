package com.banking.Controller;

import jakarta.servlet.http.HttpSession;
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

@Controller
@RequestMapping("/bank")
public class HomeController {

    @GetMapping()
    public String hello(){
        return "home/homePage";
    }

    @GetMapping("/admin")
    public String admin(){
        return "redirect:/admin/dashboard";
    }

    @Autowired
    private AccountRepository accountRepository;

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
