package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.Transaction;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceService balanceService;

    @Autowired
    private CardService cardService;

    @Autowired AccountService accountService;

    @Autowired
    private BalanceRepository balanceRepository;
    /**
     * Chuyển tiền từ cardSend sang cardReceipt
     */
    @Transactional
    public void transfer(Account acc, String CardSend,  String cardReceipt, BigDecimal amount) {
        BigDecimal balance = balanceRepository.findByAccount(acc).orElseThrow().getAvailableBalance();
        Long ReceiptId = cardService.getAccountIdByCardNumber(cardReceipt);
        Account receipt = accountRepository.findById(ReceiptId).orElseThrow(() -> new IllegalArgumentException("Thẻ nhận không tồn tại"));

        if (receipt.getAccountId().equals(acc.getAccountId())){
            throw new IllegalArgumentException("Không được tự chuyển vào chính thẻ của mình");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Số dư không đủ để thực hiện giao dịch" + " balance: "+ balance);
        }



        Balance send = accountService.findBalanceByAccount(acc);
        Balance receive = accountService.findBalanceByAccount(receipt);
        send.setAvailableBalance(send.getAvailableBalance().subtract(amount));
        receive.setAvailableBalance(receive.getAvailableBalance().add(amount));


        accountRepository.save(acc);
        accountRepository.save(receipt);

        Transaction transaction = new Transaction(LocalDate.now(),amount,"TRANSFER","SUCCESS",CardSend, cardReceipt, acc);
        transactionRepository.save(transaction);
    }
}
