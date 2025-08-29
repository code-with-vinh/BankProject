package com.banking.Repository;

import com.banking.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Thêm các phương thức tìm kiếm cho admin
    List<Account> findByEmailContainingIgnoreCase(String email);
    List<Account> findByCustomerNameContainingIgnoreCase(String name);
}
