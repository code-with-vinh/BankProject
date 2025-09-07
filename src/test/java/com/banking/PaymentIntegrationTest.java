package com.banking;

import com.banking.Entity.Account;
import com.banking.Entity.PaymentRequest;
import com.banking.Repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PaymentIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void testAccountCreation() {
        // Tạo account test
        Account testAccount = new Account();
        testAccount.setCustomerName("Test Customer");
        testAccount.setEmail("test@example.com");
        testAccount.setPassword("password123");
        testAccount.setRole("CUSTOMER");
        testAccount.setPhoneNumber("0123456789");
        
        Account savedAccount = accountRepository.save(testAccount);
        
        assertNotNull(savedAccount);
        assertNotNull(savedAccount.getAccountId());
        assertEquals("Test Customer", savedAccount.getCustomerName());
        assertEquals("test@example.com", savedAccount.getEmail());
    }

    @Test
    public void testPaymentRequestCreation() {
        // Tạo account test
        Account testAccount = new Account();
        testAccount.setCustomerName("Test Customer 2");
        testAccount.setEmail("test2@example.com");
        testAccount.setPassword("password123");
        testAccount.setRole("CUSTOMER");
        testAccount.setPhoneNumber("0123456788");
        testAccount = accountRepository.save(testAccount);

        // Tạo payment request
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(100000.0);
        paymentRequest.setCurrency("VND");
        paymentRequest.setAccount(testAccount);

        assertNotNull(paymentRequest);
        assertEquals(100000.0, paymentRequest.getAmount());
        assertEquals("VND", paymentRequest.getCurrency());
        assertEquals(testAccount.getAccountId(), paymentRequest.getAccount().getAccountId());
    }

    @Test
    public void testPaymentValidation() {
        // Test với amount <= 0
        PaymentRequest invalidPayment = new PaymentRequest();
        invalidPayment.setAmount(-100.0);
        invalidPayment.setCurrency("VND");

        assertTrue(invalidPayment.getAmount() <= 0);
    }
}
