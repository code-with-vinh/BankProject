package com.banking.Repository;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
    Optional<Balance> findByAccount(Account account);
}



