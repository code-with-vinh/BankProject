package com.banking.Service;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.Card;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.CardRepository;
import com.banking.Security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountService
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletResponse response;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;
    private Card testCard;
    private Balance testBalance;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("password123");
        testAccount.setCustomerName("Test User");
        testAccount.setRole("Customer");
        testAccount.setPhoneNumber("0123456789");
        testAccount.setLevel("SILVER");

        testCard = new Card();
        testCard.setCardId(1L);
        testCard.setCardNumber("123456789012");
        testCard.setCardType("DEBIT");
        testCard.setStatus("ACTIVE");
        testCard.setAccount(testAccount);

        testBalance = new Balance();
        testBalance.setAccountId(1L);
        testBalance.setAccount(testAccount);
        testBalance.setAvailableBalance(new BigDecimal("1000000"));
        testBalance.setHoldBalance(BigDecimal.ZERO);
    }

    /**
     * Test finding account by email when account exists
     */
    @Test
    void testFindByEmail_WhenAccountExists_ShouldReturnAccount() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.of(testAccount));

        // When
        Account result = accountService.findByEmail("test@example.com");

        // Then
        assertNotNull(result);
        assertEquals(testAccount.getEmail(), result.getEmail());
        verify(accountRepository).findByEmail("test@example.com");
    }

    /**
     * Test finding account by email when account doesn't exist
     */
    @Test
    void testFindByEmail_WhenAccountDoesNotExist_ShouldReturnNull() {
        // Given
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // When
        Account result = accountService.findByEmail("nonexistent@example.com");

        // Then
        assertNull(result);
        verify(accountRepository).findByEmail("nonexistent@example.com");
    }

    /**
     * Test finding cards by account
     */
    @Test
    void testFindCardsByAccount_ShouldReturnCardsList() {
        // Given
        List<Card> expectedCards = Arrays.asList(testCard);
        when(cardRepository.findByAccount(any(Account.class))).thenReturn(expectedCards);

        // When
        List<Card> result = accountService.findCardsByAccount(testAccount);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCard.getCardNumber(), result.get(0).getCardNumber());
        verify(cardRepository).findByAccount(testAccount);
    }

    /**
     * Test finding balance by account when balance exists
     */
    @Test
    void testFindBalanceByAccount_WhenBalanceExists_ShouldReturnBalance() {
        // Given
        when(balanceRepository.findByAccount(any(Account.class))).thenReturn(Optional.of(testBalance));

        // When
        Balance result = accountService.findBalanceByAccount(testAccount);

        // Then
        assertNotNull(result);
        assertEquals(testBalance.getAvailableBalance(), result.getAvailableBalance());
        verify(balanceRepository).findByAccount(testAccount);
    }

    /**
     * Test finding balance by account when balance doesn't exist
     */
    @Test
    void testFindBalanceByAccount_WhenBalanceDoesNotExist_ShouldReturnNull() {
        // Given
        when(balanceRepository.findByAccount(any(Account.class))).thenReturn(Optional.empty());

        // When
        Balance result = accountService.findBalanceByAccount(testAccount);

        // Then
        assertNull(result);
        verify(balanceRepository).findByAccount(testAccount);
    }

    /**
     * Test creating a DEBIT card successfully
     */
    @Test
    void testCreateCard_WithDebitCard_ShouldCreateCardSuccessfully() {
        // Given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(balanceRepository.findByAccount(any(Account.class))).thenReturn(Optional.of(testBalance));

        // When
        Card result = accountService.createCard(testAccount, "DEBIT", LocalDate.now().plusYears(3), "ACTIVE");

        // Then
        assertNotNull(result);
        assertEquals("DEBIT", result.getCardType());
        verify(accountRepository).findById(testAccount.getAccountId());
        verify(cardRepository).save(any(Card.class));
    }

    /**
     * Test creating a CREDIT card successfully
     */
    @Test
    void testCreateCard_WithCreditCard_ShouldCreateCardWithCreditLimit() {
        // Given
        testAccount.setLevel("GOLD");
        Card creditCard = new Card();
        creditCard.setCardId(2L);
        creditCard.setCardNumber("987654321098");
        creditCard.setCardType("CREDIT");
        creditCard.setStatus("ACTIVE");
        creditCard.setAccount(testAccount);
        creditCard.setCreditLimit(new BigDecimal("200000000"));
        
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
        when(cardRepository.existsByCardNumber(anyString())).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(creditCard);

        // When
        Card result = accountService.createCard(testAccount, "CREDIT", LocalDate.now().plusYears(3), "ACTIVE");

        // Then
        assertNotNull(result);
        assertEquals("CREDIT", result.getCardType());
        assertNotNull(result.getCreditLimit());
        verify(accountRepository).findById(testAccount.getAccountId());
        verify(cardRepository).save(any(Card.class));
    }

    /**
     * Test creating card when account not found
     */
    @Test
    void testCreateCard_WhenAccountNotFound_ShouldThrowException() {
        // Given
        when(accountRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.createCard(testAccount, "DEBIT", LocalDate.now().plusYears(3), "ACTIVE");
        });

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository).findById(testAccount.getAccountId());
    }

    /**
     * Test deleting card if owned by account
     */
    @Test
    void testDeleteCardIfOwned_WhenCardIsOwned_ShouldDeleteCard() {
        // Given
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(testCard));

        // When
        accountService.deleteCardIfOwned(testAccount, 1L);

        // Then
        verify(cardRepository).findById(1L);
        verify(cardRepository).delete(testCard);
    }

    /**
     * Test deleting card when not owned by account
     */
    @Test
    void testDeleteCardIfOwned_WhenCardNotOwned_ShouldNotDeleteCard() {
        // Given
        Account otherAccount = new Account();
        otherAccount.setAccountId(2L);
        testCard.setAccount(otherAccount);
        when(cardRepository.findById(anyLong())).thenReturn(Optional.of(testCard));

        // When
        accountService.deleteCardIfOwned(testAccount, 1L);

        // Then
        verify(cardRepository).findById(1L);
        verify(cardRepository, never()).delete(any(Card.class));
    }

    /**
     * Test getting balance by account ID
     */
    @Test
    void testGetBalanceByAccountId_ShouldReturnBalance() {
        // Given
        when(balanceRepository.findById(anyLong())).thenReturn(Optional.of(testBalance));

        // When
        BigDecimal result = accountService.getBalanceByAccountId(1L);

        // Then
        assertNotNull(result);
        assertEquals(testBalance.getAvailableBalance(), result);
        verify(balanceRepository).findById(1L);
    }

    /**
     * Test getting balance when balance not found
     */
    @Test
    void testGetBalanceByAccountId_WhenBalanceNotFound_ShouldThrowException() {
        // Given
        when(balanceRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            accountService.getBalanceByAccountId(1L);
        });

        assertEquals("Balance not found", exception.getMessage());
        verify(balanceRepository).findById(1L);
    }

    /**
     * Test updating email and phone successfully
     */
    @Test
    void testUpdateEmailOrPhone_WithValidData_ShouldUpdateSuccessfully() {
        // Given
        when(accountRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // When
        boolean result = accountService.updateEmailOrPhone(testAccount, "new@example.com", "0987654321");

        // Then
        assertTrue(result);
        verify(accountRepository).existsByEmail("new@example.com");
        verify(accountRepository).existsByPhoneNumber("0987654321");
        verify(accountRepository).save(testAccount);
    }

    /**
     * Test updating email when email already exists
     */
    @Test
    void testUpdateEmailOrPhone_WhenEmailExists_ShouldReturnFalse() {
        // Given
        when(accountRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        boolean result = accountService.updateEmailOrPhone(testAccount, "existing@example.com", "0987654321");

        // Then
        assertFalse(result);
        verify(accountRepository).existsByEmail("existing@example.com");
        verify(accountRepository, never()).save(any(Account.class));
    }
}
