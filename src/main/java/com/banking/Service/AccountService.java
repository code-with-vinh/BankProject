package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepo;

    public Account findByEmail(String email){
        Optional<Account> account = accountRepo.findByEmail(email);
        return account.orElse(null);
    }

}
