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

    public Account findByEmail(String email){
        Optional<Account> account = accountRepo.findByEmail(email);
        return account.orElse(null);
    }

    public List<Card> findCardsByAccount(Account account) {
        return cardRepository.findByAccount(account);
    }

    public Balance findBalanceByAccount(Account account) {
        return balanceRepository.findByAccount(account).orElse(null);
    }

    public Card createCard(Account account, String cardType, LocalDate expiryDate, String status) {
        Card card = new Card();
        card.setAccount(account);
        card.setCardType(cardType);
        card.setExpiryDate(expiryDate);
        card.setStatus(status);
        if ("CREDIT".equalsIgnoreCase(cardType)) {
            BigDecimal defaultLimit = defaultCreditLimitForLevel(account.getLevel());
            card.setCreditLimit(defaultLimit);
        } else {
            card.setCreditLimit(null);
            // Ensure balance exists for DEBIT accounts; initialize to 0 if absent
            balanceRepository.findByAccount(account).orElseGet(() -> {
                Balance b = new Balance();
                b.setAccount(account);
                b.setAvailableBalance(new BigDecimal("0"));
                b.setHoldBalance(new BigDecimal("0"));
                return balanceRepository.save(b);
            });
        }
        card.setCardNumber(generateUniqueCardNumber());
        return cardRepository.save(card);
    }

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

    public void deleteCardIfOwned(Account account, Long cardId) {
        cardRepository.findById(cardId).ifPresent(card -> {
            if (card.getAccount().getAccountId().equals(account.getAccountId())) {
                cardRepository.delete(card);
            }
        });
    }

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
