package com.banking.Integration;

import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.Card;
import com.banking.Entity.PaymentRequest;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.CardRepository;
import com.banking.Repository.PaymentRequestRepository;
import com.banking.Service.AccountService;
import com.banking.Service.AuthService;
import com.banking.Service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Banking System
 * 
 * These tests verify the integration between different components
 * using the actual database layer.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BankingIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private PaymentService paymentService;

    private Account testAccount;

    @BeforeEach
    void setUp() {
        // Create a test account
        testAccount = new Account();
        testAccount.setEmail("integration@test.com");
        testAccount.setPassword("password123");
        testAccount.setCustomerName("Integration Test User");
        testAccount.setRole("Customer");
        testAccount.setPhoneNumber("0123456789");
        testAccount.setLevel("SILVER");
        
        testAccount = accountRepository.save(testAccount);
    }

    /**
     * Test complete user registration flow
     */
    @Test
    void testCompleteUserRegistrationFlow() {
        // Given
        Account newAccount = new Account();
        newAccount.setEmail("newuser@test.com");
        newAccount.setPassword("password123");
        newAccount.setCustomerName("New User");
        newAccount.setPhoneNumber("0987654321");

        // When
        authService.register(newAccount);

        // Then
        Optional<Account> savedAccount = accountRepository.findByEmail("newuser@test.com");
        assertTrue(savedAccount.isPresent());
        assertEquals("Customer", savedAccount.get().getRole());
        assertEquals("SILVER", savedAccount.get().getLevel());
    }

    /**
     * Test complete login flow
     */
    @Test
    void testCompleteLoginFlow() {
        // When
        Account loggedInAccount = authService.login("integration@test.com", "password123");

        // Then
        assertNotNull(loggedInAccount);
        assertEquals("integration@test.com", loggedInAccount.getEmail());
        assertEquals("Integration Test User", loggedInAccount.getCustomerName());
    }

    /**
     * Test complete card creation flow
     */
    @Test
    void testCompleteCardCreationFlow() {
        // Given
        LocalDate expiryDate = LocalDate.now().plusYears(3);

        // When
        Card createdCard = accountService.createCard(testAccount, "DEBIT", expiryDate, "ACTIVE");

        // Then
        assertNotNull(createdCard);
        assertEquals("DEBIT", createdCard.getCardType());
        assertEquals("ACTIVE", createdCard.getStatus());
        assertEquals(testAccount.getAccountId(), createdCard.getAccount().getAccountId());
        assertNotNull(createdCard.getCardNumber());
        assertEquals(12, createdCard.getCardNumber().length());

        // Verify balance was created for DEBIT card
        Optional<Balance> balance = balanceRepository.findByAccount(testAccount);
        assertTrue(balance.isPresent());
        assertEquals(BigDecimal.ZERO, balance.get().getAvailableBalance());
    }

    /**
     * Test complete credit card creation flow
     */
    @Test
    void testCompleteCreditCardCreationFlow() {
        // Given
        testAccount.setLevel("GOLD");
        testAccount = accountRepository.save(testAccount);
        LocalDate expiryDate = LocalDate.now().plusYears(3);

        // When
        Card createdCard = accountService.createCard(testAccount, "CREDIT", expiryDate, "ACTIVE");

        // Then
        assertNotNull(createdCard);
        assertEquals("CREDIT", createdCard.getCardType());
        assertNotNull(createdCard.getCreditLimit());
        // GOLD level should have 200,000,000 credit limit
        assertEquals(new BigDecimal("200000000"), createdCard.getCreditLimit());
    }

    /**
     * Test complete payment request creation and payment flow
     */
    @Test
    void testCompletePaymentFlow() {
        // Given
        // Create a DEBIT card and balance
        Card debitCard = accountService.createCard(testAccount, "DEBIT", LocalDate.now().plusYears(3), "ACTIVE");
        
        // Add some balance
        Balance balance = balanceRepository.findByAccount(testAccount).orElseThrow();
        balance.setAvailableBalance(new BigDecimal("1000000"));
        balanceRepository.save(balance);

        // Create payment request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAccount(testAccount);
        paymentRequest.setAmount(500000.0);
        paymentRequest.setCurrency("VND");
        paymentRequest.setDescription("Test payment");
        paymentRequest.setStatus(PaymentRequest.PaymentStatus.PENDING);
        paymentRequest.setCreatedAt(java.time.LocalDateTime.now());
        
        paymentRequest = paymentRequestRepository.save(paymentRequest);

        // When - Process payment
        PaymentRequest processedPayment = paymentService.processPayment(paymentRequest);

        // Then
        assertNotNull(processedPayment);
        assertEquals(500000.0, processedPayment.getAmount());
        assertEquals("VND", processedPayment.getCurrency());
    }

    /**
     * Test account level update flow
     */
    @Test
    void testAccountLevelUpdateFlow() {
        // Given
        String newLevel = "GOLD";

        // When
        testAccount.setLevel(newLevel);
        testAccount = accountRepository.save(testAccount);

        // Then
        Optional<Account> updatedAccount = accountRepository.findById(testAccount.getAccountId());
        assertTrue(updatedAccount.isPresent());
        assertEquals(newLevel, updatedAccount.get().getLevel());
    }

    /**
     * Test card deletion flow
     */
    @Test
    void testCardDeletionFlow() {
        // Given
        Card card = accountService.createCard(testAccount, "DEBIT", LocalDate.now().plusYears(3), "ACTIVE");
        Long cardId = card.getCardId();

        // When
        accountService.deleteCardIfOwned(testAccount, cardId);

        // Then
        Optional<Card> deletedCard = cardRepository.findById(cardId);
        assertFalse(deletedCard.isPresent());
    }

    /**
     * Test multiple cards for one account
     */
    @Test
    void testMultipleCardsForOneAccount() {
        // Given
        Card debitCard = accountService.createCard(testAccount, "DEBIT", LocalDate.now().plusYears(3), "ACTIVE");
        Card creditCard = accountService.createCard(testAccount, "CREDIT", LocalDate.now().plusYears(3), "ACTIVE");

        // When
        List<Card> cards = accountService.findCardsByAccount(testAccount);

        // Then
        assertEquals(2, cards.size());
        assertTrue(cards.stream().anyMatch(c -> "DEBIT".equals(c.getCardType())));
        assertTrue(cards.stream().anyMatch(c -> "CREDIT".equals(c.getCardType())));
    }

    /**
     * Test payment request retrieval by account
     */
    @Test
    void testPaymentRequestRetrievalByAccount() {
        // Given
        PaymentRequest payment1 = new PaymentRequest();
        payment1.setAccount(testAccount);
        payment1.setAmount(100000.0);
        payment1.setCurrency("VND");
        payment1.setStatus(PaymentRequest.PaymentStatus.PENDING);
        payment1.setCreatedAt(java.time.LocalDateTime.now());
        paymentRequestRepository.save(payment1);

        PaymentRequest payment2 = new PaymentRequest();
        payment2.setAccount(testAccount);
        payment2.setAmount(200000.0);
        payment2.setCurrency("VND");
        payment2.setStatus(PaymentRequest.PaymentStatus.PENDING);
        payment2.setCreatedAt(java.time.LocalDateTime.now());
        paymentRequestRepository.save(payment2);

        // When
        List<PaymentRequest> payments = paymentService.getPaymentRequestsByAccount(testAccount.getAccountId());

        // Then
        assertEquals(2, payments.size());
        assertTrue(payments.stream().allMatch(p -> testAccount.getAccountId().equals(p.getAccount().getAccountId())));
    }

    /**
     * Test account email update flow
     */
    @Test
    void testAccountEmailUpdateFlow() {
        // Given
        String newEmail = "updated@test.com";

        // When
        boolean updated = accountService.updateEmailOrPhone(testAccount, newEmail, null);

        // Then
        assertTrue(updated);
        Optional<Account> updatedAccount = accountRepository.findById(testAccount.getAccountId());
        assertTrue(updatedAccount.isPresent());
        assertEquals(newEmail, updatedAccount.get().getEmail());
    }

    /**
     * Test account phone update flow
     */
    @Test
    void testAccountPhoneUpdateFlow() {
        // Given
        String newPhone = "0987654321";

        // When
        boolean updated = accountService.updateEmailOrPhone(testAccount, null, newPhone);

        // Then
        assertTrue(updated);
        Optional<Account> updatedAccount = accountRepository.findById(testAccount.getAccountId());
        assertTrue(updatedAccount.isPresent());
        assertEquals(newPhone, updatedAccount.get().getPhoneNumber());
    }
}
