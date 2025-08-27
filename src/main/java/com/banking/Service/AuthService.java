package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private AccountService accountService;

    public boolean accountExist(Account account) {
        return accountRepo.findByEmail(account.getEmail()).isPresent();
    }

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
    public Account login(String email, String password){
        Account acc = accountService.findByEmail(email);
        if(acc == null || !acc.getPassword().equals(password)){
            return null;
        }
        return acc;
    }
}
