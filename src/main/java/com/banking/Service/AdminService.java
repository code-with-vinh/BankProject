package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.Card;
import com.banking.Entity.Transaction;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.CardRepository;
import com.banking.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    private PasswordEncoder passwordEncoder;

    // Lấy tổng số tài khoản
    public long getTotalAccounts() {
        return accountRepository.count();
    }

    // Lấy tổng số thẻ
    public long getTotalCards() {
        return cardRepository.count();
    }

    // Lấy tổng số giao dịch
    public long getTotalTransactions() {
        return transactionRepository.count();
    }

    // Lấy tất cả tài khoản
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    // Lấy tất cả thẻ
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    // Lấy tất cả giao dịch
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // Lấy tài khoản theo ID
    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId).orElse(null);
    }

    // Lấy thẻ theo tài khoản
    public List<Card> getCardsByAccount(Long accountId) {
        Account account = getAccountById(accountId);
        if (account != null) {
            return cardRepository.findByAccount(account);
        }
        return List.of();
    }

    // Lấy giao dịch theo tài khoản
    public List<Transaction> getTransactionsByAccount(Long accountId) {
        Account account = getAccountById(accountId);
        if (account != null) {
            return transactionRepository.findByAccount(account);
        }
        return List.of();
    }

    // Tạo user mới
    public void createUser(String customerName, String email, String password, String role, String phoneNumber) {
        // Kiểm tra email đã tồn tại
        if (accountRepository.existsByEmail(email)) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (accountRepository.existsByPhoneNumber(phoneNumber)) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        // Kiểm tra role hợp lệ
        if (!role.equals("Customer") && !role.equals("Admin")) {
            throw new RuntimeException("Role không hợp lệ");
        }

        // Tạo tài khoản mới
        Account account = new Account();
        account.setCustomerName(customerName);
        account.setEmail(email);
        account.setPassword(passwordEncoder.encode(password));
        account.setRole(role);
        account.setPhoneNumber(phoneNumber);
        account.setLevel("SILVER"); // Mặc định level SILVER

        accountRepository.save(account);
    }


    // Xóa tài khoản
    public void deleteAccount(Long accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            
            // Kiểm tra xem tài khoản có thẻ không
            List<Card> cards = cardRepository.findByAccount(account);
            if (!cards.isEmpty()) {
                throw new RuntimeException("Không thể xóa tài khoản có thẻ");
            }
            
            accountRepository.delete(account);
        }
    }

    // Cập nhật role của user
    public void updateUserRole(Long accountId, String role) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            
            // Kiểm tra role hợp lệ
            if (!role.equals("Customer") && !role.equals("Admin")) {
                throw new RuntimeException("Role không hợp lệ");
            }
            
            account.setRole(role);
            accountRepository.save(account);
        }
    }

    // Cập nhật level của user
    public void updateUserLevel(Long accountId, String level) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            
            // Kiểm tra level hợp lệ
            if (!level.equals("SILVER") && !level.equals("GOLD") && !level.equals("PLATINUM")) {
                throw new RuntimeException("Level không hợp lệ");
            }
            
            account.setLevel(level);
            accountRepository.save(account);
        }
    }

    // Cập nhật trạng thái thẻ
    public void updateCardStatus(Long cardId, String status) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isPresent()) {
            Card card = cardOpt.get();
            
            // Kiểm tra trạng thái hợp lệ
            if (!status.equals("ACTIVE") && !status.equals("INACTIVE") && !status.equals("EXPIRED")) {
                throw new RuntimeException("Trạng thái không hợp lệ");
            }
            
            card.setStatus(status);
            cardRepository.save(card);
        }
    }

    // Xóa thẻ
    public void deleteCard(Long cardId) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (cardOpt.isPresent()) {
            cardRepository.delete(cardOpt.get());
        }
    }

    // Tìm kiếm tài khoản theo email
    public List<Account> searchAccountsByEmail(String email) {
        return accountRepository.findByEmailContainingIgnoreCase(email);
    }

    // Tìm kiếm tài khoản theo tên
    public List<Account> searchAccountsByName(String name) {
        return accountRepository.findByCustomerNameContainingIgnoreCase(name);
    }

    // Nạp tiền vào tài khoản debit của thẻ
    public void depositToCard(Long cardId, String amount) {
        Optional<Card> cardOpt = cardRepository.findById(cardId);
        if (!cardOpt.isPresent()) {
            throw new RuntimeException("Thẻ không tồn tại");
        }

        Card card = cardOpt.get();
        Account account = card.getAccount();
        
        // Chỉ cho phép nạp tiền vào thẻ debit
        if (!"DEBIT".equals(card.getCardType())) {
            throw new RuntimeException("Chỉ có thể nạp tiền vào thẻ debit");
        }

        // Kiểm tra số tiền hợp lệ
        BigDecimal depositAmount;
        try {
            depositAmount = new BigDecimal(amount);
            if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Số tiền phải lớn hơn 0");
            }
            if (depositAmount.compareTo(new BigDecimal("10000")) < 0) {
                throw new RuntimeException("Số tiền tối thiểu phải là 10,000 VND");
            }
        } catch (NumberFormatException e) {
            throw new RuntimeException("Số tiền không hợp lệ");
        }

        // Tìm hoặc tạo balance cho tài khoản
        Balance balance = balanceRepository.findByAccount(account).orElse(null);
        if (balance == null) {
            // Tạo balance mới nếu chưa có
            balance = new Balance();
            balance.setAccount(account);
            balance.setAvailableBalance(BigDecimal.ZERO);
            balance.setHoldBalance(BigDecimal.ZERO);
        }

        // Cập nhật số dư
        BigDecimal currentBalance = balance.getAvailableBalance() != null ? balance.getAvailableBalance() : BigDecimal.ZERO;
        balance.setAvailableBalance(currentBalance.add(depositAmount));
        
        // Lưu balance
        balanceRepository.save(balance);


    }
}
