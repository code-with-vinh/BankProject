package com.banking.Repository;

import com.banking.Entity.Account;
import com.banking.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccount(Account account);
    List<Transaction> findByAccountOrderByTransactionDateDesc(Account account);

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.account.accountId = :accountId AND t.type = 'DEPOSIT' AND t.status = 'SUCCESS'")
    BigDecimal getTotalDeposit(Long accountId);

    @Query("SELECT COALESCE(SUM(t.amount), 0) " +
            "FROM Transaction t " +
            "WHERE t.account.accountId = :accountId AND t.type = 'WITHDRAW' AND t.status = 'SUCCESS'")
    BigDecimal getTotalWithdraw(Long accountId);

}
