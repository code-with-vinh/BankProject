package com.banking.Service;

import com.banking.DTO.CreatePaymentRequestDTO;
import com.banking.DTO.PayPaymentRequestDTO;
import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Entity.PaymentRequest;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.PaymentRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;

import jakarta.jms.Queue;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private JmsTemplate jmsTemplate;

    @Mock
    private Queue paymentQueue;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BalanceRepository balanceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Account testAccount;
    private PaymentRequest testPaymentRequest;
    private Balance testBalance;
    private CreatePaymentRequestDTO createDTO;
    private PayPaymentRequestDTO payDTO;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setAccountId(1L);
        testAccount.setEmail("test@example.com");
        testAccount.setCustomerName("Test User");
        testAccount.setRole("Customer");

        testBalance = new Balance();
        testBalance.setAccountId(1L);
        testBalance.setAccount(testAccount);
        testBalance.setAvailableBalance(new BigDecimal("1000000"));
        testBalance.setHoldBalance(BigDecimal.ZERO);

        testPaymentRequest = new PaymentRequest();
        testPaymentRequest.setPaymentId(1L);
        testPaymentRequest.setAccount(testAccount);
        testPaymentRequest.setAmount(500000.0);
        testPaymentRequest.setCurrency("VND");
        testPaymentRequest.setDescription("Test payment");
        testPaymentRequest.setStatus(PaymentRequest.PaymentStatus.PENDING);
        testPaymentRequest.setCreatedAt(LocalDateTime.now());

        createDTO = new CreatePaymentRequestDTO();
        createDTO.setAccountId(1L);
        createDTO.setAmount(500000.0);
        createDTO.setCurrency("VND");
        createDTO.setDescription("Test payment request");

        payDTO = new PayPaymentRequestDTO();
        payDTO.setPaymentId(1L);
        payDTO.setAccountId(1L);
    }

    /**
     * Test successful payment processing
     */
    @Test
    void testProcessPayment_WithValidData_ShouldProcessSuccessfully() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(true);

        // When
        PaymentRequest result = paymentService.processPayment(testPaymentRequest);

        // Then
        assertNotNull(result);
        assertEquals(testPaymentRequest.getAmount(), result.getAmount());
        verify(accountRepository).existsById(testPaymentRequest.getAccount().getAccountId());
    }

    /**
     * Test payment processing when account not found
     */
    @Test
    void testProcessPayment_WhenAccountNotFound_ShouldThrowException() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(testPaymentRequest);
        });

        assertTrue(exception.getMessage().contains("Account not found"));
        verify(accountRepository).existsById(testPaymentRequest.getAccount().getAccountId());
        verify(notificationService).handlePaymentFailure(any(PaymentRequest.class), anyString());
    }

    /**
     * Test payment processing with invalid amount
     */
    @Test
    void testProcessPayment_WithInvalidAmount_ShouldThrowException() {
        // Given
        testPaymentRequest.setAmount(-100.0);
        when(accountRepository.existsById(anyLong())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(testPaymentRequest);
        });

        assertTrue(exception.getMessage().contains("Amount must be greater than 0"));
        verify(notificationService).handlePaymentFailure(any(PaymentRequest.class), anyString());
    }

    /**
     * Test payment processing with insufficient balance
     */
    @Test
    void testProcessPayment_WithInsufficientBalance_ShouldThrowException() {
        // Given
        testPaymentRequest.setAmount(2000000.0); // Amount > 1000000 limit
        when(accountRepository.existsById(anyLong())).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.processPayment(testPaymentRequest);
        });

        assertTrue(exception.getMessage().contains("Insufficient balance"));
        verify(notificationService).handlePaymentFailure(any(PaymentRequest.class), anyString());
    }

    /**
     * Test getting payment by ID when payment exists
     */
    @Test
    void testGetPaymentById_WhenPaymentExists_ShouldReturnPayment() {
        // Given
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPaymentRequest));

        // When
        PaymentRequest result = paymentService.getPaymentById(1L);

        // Then
        assertNotNull(result);
        assertEquals(testPaymentRequest.getPaymentId(), result.getPaymentId());
        verify(paymentRequestRepository).findById(1L);
    }

    /**
     * Test getting payment by ID when payment doesn't exist
     */
    @Test
    void testGetPaymentById_WhenPaymentDoesNotExist_ShouldReturnNull() {
        // Given
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        PaymentRequest result = paymentService.getPaymentById(1L);

        // Then
        assertNull(result);
        verify(paymentRequestRepository).findById(1L);
    }

    /**
     * Test creating payment request successfully
     */
    @Test
    void testCreatePaymentRequest_WithValidData_ShouldCreateSuccessfully() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(true);
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenReturn(testPaymentRequest);

        // When
        PaymentRequest result = paymentService.createPaymentRequest(createDTO);

        // Then
        assertNotNull(result);
        assertEquals(createDTO.getAmount(), result.getAmount());
        assertEquals(createDTO.getCurrency(), result.getCurrency());
        assertEquals(PaymentRequest.PaymentStatus.PENDING, result.getStatus());
        verify(accountRepository).existsById(createDTO.getAccountId());
        verify(accountRepository).findById(createDTO.getAccountId());
        verify(paymentRequestRepository).save(any(PaymentRequest.class));
        verify(notificationService).sendPaymentRequestNotification(any(PaymentRequest.class));
    }

    /**
     * Test creating payment request when account not found
     */
    @Test
    void testCreatePaymentRequest_WhenAccountNotFound_ShouldThrowException() {
        // Given
        when(accountRepository.existsById(anyLong())).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.createPaymentRequest(createDTO);
        });

        assertEquals("Failed to create payment request: Account not found", exception.getMessage());
        verify(accountRepository).existsById(createDTO.getAccountId());
    }

    /**
     * Test paying payment request successfully
     */
    @Test
    void testPayPaymentRequest_WithValidData_ShouldPaySuccessfully() {
        // Given
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPaymentRequest));
        when(balanceRepository.findByAccount(any(Account.class))).thenReturn(Optional.of(testBalance));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenReturn(testPaymentRequest);

        // When
        PaymentRequest result = paymentService.payPaymentRequest(payDTO);

        // Then
        assertNotNull(result);
        assertEquals(PaymentRequest.PaymentStatus.PAID, result.getStatus());
        assertNotNull(result.getPaidAt());
        verify(paymentRequestRepository).findById(payDTO.getPaymentId());
        verify(balanceRepository).findByAccount(testAccount);
        verify(paymentRequestRepository).save(any(PaymentRequest.class));
    }

    /**
     * Test paying payment request when payment not found
     */
    @Test
    void testPayPaymentRequest_WhenPaymentNotFound_ShouldThrowException() {
        // Given
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.payPaymentRequest(payDTO);
        });

        assertEquals("Failed to process payment: Payment request not found", exception.getMessage());
        verify(paymentRequestRepository).findById(payDTO.getPaymentId());
    }

    /**
     * Test paying payment request when not pending
     */
    @Test
    void testPayPaymentRequest_WhenNotPending_ShouldThrowException() {
        // Given
        testPaymentRequest.setStatus(PaymentRequest.PaymentStatus.PAID);
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPaymentRequest));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.payPaymentRequest(payDTO);
        });

        assertEquals("Failed to process payment: Payment request is not pending", exception.getMessage());
        verify(paymentRequestRepository).findById(payDTO.getPaymentId());
    }

    /**
     * Test paying payment request with insufficient balance
     */
    @Test
    void testPayPaymentRequest_WithInsufficientBalance_ShouldThrowException() {
        // Given
        testBalance.setAvailableBalance(new BigDecimal("100000")); // Less than payment amount
        when(paymentRequestRepository.findById(anyLong())).thenReturn(Optional.of(testPaymentRequest));
        when(balanceRepository.findByAccount(any(Account.class))).thenReturn(Optional.of(testBalance));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenReturn(testPaymentRequest);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            paymentService.payPaymentRequest(payDTO);
        });

        assertEquals("Failed to process payment: Insufficient available balance", exception.getMessage());
        verify(paymentRequestRepository).findById(payDTO.getPaymentId());
        verify(balanceRepository).findByAccount(testAccount);
    }

    /**
     * Test getting payment requests by account
     */
    @Test
    void testGetPaymentRequestsByAccount_ShouldReturnPaymentRequests() {
        // Given
        List<PaymentRequest> expectedPayments = Arrays.asList(testPaymentRequest);
        when(paymentRequestRepository.findByAccountIdOrderByCreatedAtDesc(anyLong())).thenReturn(expectedPayments);

        // When
        List<PaymentRequest> result = paymentService.getPaymentRequestsByAccount(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPaymentRequest.getPaymentId(), result.get(0).getPaymentId());
        verify(paymentRequestRepository).findByAccountIdOrderByCreatedAtDesc(1L);
    }

    /**
     * Test getting pending payment requests
     */
    @Test
    void testGetPendingPaymentRequests_ShouldReturnPendingPayments() {
        // Given
        List<PaymentRequest> expectedPayments = Arrays.asList(testPaymentRequest);
        when(paymentRequestRepository.findPendingPayments()).thenReturn(expectedPayments);

        // When
        List<PaymentRequest> result = paymentService.getPendingPaymentRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRequestRepository).findPendingPayments();
    }

    /**
     * Test getting all payment requests
     */
    @Test
    void testGetAllPaymentRequests_ShouldReturnAllPayments() {
        // Given
        List<PaymentRequest> expectedPayments = Arrays.asList(testPaymentRequest);
        when(paymentRequestRepository.findAll()).thenReturn(expectedPayments);

        // When
        List<PaymentRequest> result = paymentService.getAllPaymentRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRequestRepository).findAll();
    }
}
