package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.Card;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.CardRepository;
import com.banking.Security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.time.LocalDate;
import java.util.Random;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Account Service
 * 
 * Handles all account-related operations including account management,
 * card creation, balance management, and user profile updates.
 * Provides caching support for improved performance.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private JwtUtil jwtUtil;


    /**
     * Find an account by email address
     * 
     * @param email The email address to search for
     * @return Account object if found, null otherwise
     */
    public Account findByEmail(String email){
        Optional<Account> account = accountRepo.findByEmail(email);
        return account.orElse(null);
    }

    /**
     * Find all cards associated with an account
     * 
     * @param account The account to search for cards
     * @return List of cards associated with the account
     */
    public List<Card> findCardsByAccount(Account account) {
        return cardRepository.findByAccount(account);
    }

    /**
     * Find the balance associated with an account
     * 
     * @param account The account to search for balance
     * @return Balance object if found, null otherwise
     */
    public Balance findBalanceByAccount(Account account) {
        return balanceRepository.findByAccount(account).orElse(null);
    }

    /**
     * Create a new card for an account
     * 
     * @param account The account to create the card for
     * @param cardType The type of card (CREDIT or DEBIT)
     * @param expiryDate The expiry date of the card
     * @param status The initial status of the card
     * @return The created card
     * @throws RuntimeException if account not found
     */
    public Card createCard(Account account, String cardType, LocalDate expiryDate, String status) {
        // Always get account from DB to avoid Detached entity issues
        Account managedAccount = accountRepo.findById(account.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Card card = new Card();
        card.setAccount(managedAccount); // Use managed entity
        card.setCardType(cardType);
        card.setExpiryDate(expiryDate);
        card.setStatus(status);

        if ("CREDIT".equalsIgnoreCase(cardType)) {
            BigDecimal defaultLimit = defaultCreditLimitForLevel(managedAccount.getLevel());
            card.setCreditLimit(defaultLimit);
        } else {
            card.setCreditLimit(null);

            // Ensure balance exists for DEBIT accounts; initialize to 0 if absent
            balanceRepository.findByAccount(managedAccount).orElseGet(() -> {
                Balance b = new Balance();
                b.setAccount(managedAccount);
                b.setAvailableBalance(BigDecimal.ZERO);
                b.setHoldBalance(BigDecimal.ZERO);
                return balanceRepository.save(b);
            });
        }
        card.setCardNumber(generateUniqueCardNumber());
        return cardRepository.save(card);
    }

    /**
     * Generate a unique 12-digit card number
     * 
     * @return A unique card number that doesn't exist in the database
     */
    private String generateUniqueCardNumber() {
        Random random = new Random();
        String number;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 12; i++) {
                sb.append(random.nextInt(10));
            }
            number = sb.toString();
        } while (cardRepository.existsByCardNumber(number));
        return number;
    }

    /**
     * Delete a card if it belongs to the specified account
     * 
     * @param account The account that should own the card
     * @param cardId The ID of the card to delete
     */
    public void deleteCardIfOwned(Account account, Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            if (card.getAccount().getAccountId().equals(account.getAccountId())) {
                cardRepository.delete(card);
            }
        });
    }

    /**
     * Get the default credit limit based on account level
     * 
     * @param level The account level (SILVER, GOLD, PLATINUM)
     * @return The credit limit amount
     */
    private BigDecimal defaultCreditLimitForLevel(String level) {
        if (level == null) return new BigDecimal("50000000");
        switch (level.toUpperCase()) {
            case "GOLD":
                return new BigDecimal("200000000");
            case "PLATINUM":
                return new BigDecimal("2000000000");
            default:
                return new BigDecimal("50000000");
        }
    }
    
    /**
     * Get the available balance for an account
     * 
     * @param accountId The account ID
     * @return The available balance amount
     * @throws RuntimeException if balance not found
     */
    public BigDecimal getBalanceByAccountId(Long accountId) {
        Balance balance = balanceRepository.findById(accountId).orElseThrow(() -> new RuntimeException("Balance not found"));
        return balance.getAvailableBalance();
    }

    public boolean updateEmailOrPhone(Account account, String newEmail, String newPhone) {
        boolean changed = false;
        if (newEmail != null && !Objects.equals(newEmail, account.getEmail())) {
            if (accountRepo.existsByEmail(newEmail)) {
                return false;
            }
            account.setEmail(newEmail);
            changed = true;
        }
        if (newPhone != null && !Objects.equals(newPhone, account.getPhoneNumber())) {
            if (accountRepo.existsByPhoneNumber(newPhone)) {
                return false;
            }
            account.setPhoneNumber(newPhone);
            changed = true;
        }
        if (changed) {
            accountRepo.save(account);
        }
        return changed;
    }

    public void updateAccountInfo(String newEmail, String newPhone, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UsernameNotFoundException("User not logged in");
        }

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }

        Account acc = findByEmail(email);
        if (acc == null) {
            throw new IllegalArgumentException("Account not found");
        }

        String oldEmail = acc.getEmail();
        boolean ok = updateEmailOrPhone(acc, newEmail, newPhone);
        if (!ok) {
            throw new UsernameNotFoundException("Email hoặc SĐT đã tồn tại");
        }

        // Nếu đổi email thì phải refresh JWT và SecurityContext
        if (oldEmail != null && !oldEmail.equals(acc.getEmail())) {
            String token = jwtUtil.generateToken(acc.getEmail(), acc.getRole());

            Cookie jwtCookie = new Cookie("JWT", token);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + acc.getRole());
            UsernamePasswordAuthenticationToken newAuth =
                    new UsernamePasswordAuthenticationToken(acc.getEmail(), null, List.of(authority));
            SecurityContextHolder.getContext().setAuthentication(newAuth);
        }
    }
    public boolean deleteAccountIfNoCardsAndZeroBalance(Account account) {
        List<Card> cards = cardRepository.findByAccount(account);
        if (cards != null && !cards.isEmpty()) {
            return false;
        }
        Balance bal = balanceRepository.findByAccount(account).orElse(null);
        if (bal != null) {
            if (bal.getAvailableBalance() == null || bal.getHoldBalance() == null) {
                return false;
            }
            if (bal.getAvailableBalance().compareTo(BigDecimal.ZERO) != 0 ||
                    bal.getHoldBalance().compareTo(BigDecimal.ZERO) != 0) {
                return false;
            }
        }
        accountRepo.delete(account);
        return true;
    }

}
