package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Authentication Service
 * 
 * Handles user authentication operations including registration and login.
 * Manages account validation and user creation.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AuthService {
    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private AccountService accountService;

    /**
     * Check if an account with the given email already exists
     * 
     * @param account The account to check
     * @return true if account exists, false otherwise
     */
    public boolean accountExist(Account account) {
        return accountRepo.findByEmail(account.getEmail()).isPresent();
    }

    /**
     * Register a new user account
     * 
     * @param account The account information to register
     * @throws RuntimeException if account already exists
     */
    public void register(Account account) {
        Account acc = new Account();
        if (accountExist(account)) {
            throw new RuntimeException("Tài khoản đã tồn tại, vui lòng chọn username khác!");
        }else{
            acc.setEmail(account.getEmail());
            acc.setCustomerName(account.getCustomerName());
            acc.setPassword(account.getPassword());
            acc.setRole("Customer");
            acc.setPhoneNumber(account.getPhoneNumber());
            accountRepo.save(acc);
        }
    }
    
    /**
     * Authenticate user login
     * 
     * @param email User's email address
     * @param password User's password
     * @return Account object if login successful, null otherwise
     */
    public Account login(String email, String password){
        Account acc = accountService.findByEmail(email);
        if(acc == null || !acc.getPassword().equals(password)){
            return null;
        }
        return acc;
    }
}
