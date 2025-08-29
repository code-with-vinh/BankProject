package com.banking.Repository;

import com.banking.Entity.Account;
import com.banking.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByAccount(Account account);
    boolean existsByCardNumber(String cardNumber);
    Optional<Card> findByCardNumber(String cardNumber);
}


