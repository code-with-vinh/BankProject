package com.banking.Service;

import com.banking.Repository.BalanceRepository;
import com.banking.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class BalanceService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceRepository balanceRepository;
    public BigDecimal getBalanceByAccountId(Long accountId) {
        return balanceRepository.findById(accountId).orElseThrow().getAvailableBalance();
    }


}
