package com.banking.Service;

import com.banking.DTO.CreatePaymentRequestDTO;
import com.banking.DTO.PayPaymentRequestDTO;
import com.banking.Entity.PaymentRequest;
import com.banking.Entity.Account;
import com.banking.Entity.Balance;
import com.banking.Repository.BalanceRepository;
import com.banking.Repository.AccountRepository;
import com.banking.Repository.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import jakarta.jms.Queue;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Service
 * 
 * Handles all payment-related operations including payment processing,
 * payment request creation, and payment status management.
 * Integrates with JMS for asynchronous payment processing.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@Service
public class PaymentService {

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private Queue paymentQueue;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRequestRepository paymentRequestRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BalanceRepository balanceRepository;

    /**
     * Xử lý yêu cầu thanh toán và gửi message đến Message Queue
     * @param paymentRequest Thông tin yêu cầu thanh toán
     * @return PaymentRequest đã được lưu và gửi đến queue
     */
    public PaymentRequest processPayment(PaymentRequest paymentRequest) {
        try {
            // Kiểm tra tài khoản có tồn tại không
            if (paymentRequest.getAccount() == null || 
                !accountRepository.existsById(paymentRequest.getAccount().getAccountId())) {
                String errorMsg = "Account not found";
                notificationService.handlePaymentFailure(paymentRequest, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            // Validate dữ liệu
            if (paymentRequest.getAmount() <= 0) {
                String errorMsg = "Amount must be greater than 0";
                notificationService.handlePaymentFailure(paymentRequest, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            if (paymentRequest.getCurrency() == null || paymentRequest.getCurrency().trim().isEmpty()) {
                paymentRequest.setCurrency("VND");
            }

            // Giả lập xử lý thanh toán (kiểm tra số dư, etc.)
            if (paymentRequest.getAmount() > 1000000) {
                String errorMsg = "Insufficient balance";
                notificationService.handlePaymentFailure(paymentRequest, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            // Gửi message đến Message Queue (tạm thời comment để test)
            // jmsTemplate.convertAndSend(paymentQueue, paymentRequest);
            
            System.out.println("Payment request processed (JMS disabled for testing): " + paymentRequest);
            
            return paymentRequest;
            
        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            
            // Gửi thông báo lỗi nếu chưa gửi
            if (!e.getMessage().contains("Insufficient balance") && 
                !e.getMessage().contains("Amount must be greater than 0") &&
                !e.getMessage().contains("Account not found")) {
                notificationService.handlePaymentFailure(paymentRequest, "System error: " + e.getMessage());
            }
            
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin thanh toán theo ID
     * @param paymentId ID của thanh toán
     * @return PaymentRequest
     */
    public PaymentRequest getPaymentById(Long paymentId) {
        return paymentRequestRepository.findById(paymentId).orElse(null);
    }

    /**
     * Admin tạo payment request cho customer
     * @param createDTO Thông tin tạo payment request
     * @return PaymentRequest đã được tạo
     */
    public PaymentRequest createPaymentRequest(CreatePaymentRequestDTO createDTO) {
        try {
            // Kiểm tra account có tồn tại không
            if (!accountRepository.existsById(createDTO.getAccountId())) {
                throw new RuntimeException("Account not found");
            }

            // Tạo PaymentRequest mới
            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(createDTO.getAmount());
            paymentRequest.setCurrency(createDTO.getCurrency());
            paymentRequest.setDescription(createDTO.getDescription());
            paymentRequest.setStatus(PaymentRequest.PaymentStatus.PENDING);
            paymentRequest.setCreatedAt(LocalDateTime.now());

            // Lấy account từ database
            com.banking.Entity.Account account = accountRepository.findById(createDTO.getAccountId()).orElse(null);
            paymentRequest.setAccount(account);

            // Lưu vào database
            PaymentRequest savedPayment = paymentRequestRepository.save(paymentRequest);

            // Gửi email thông báo cho customer
            notificationService.sendPaymentRequestNotification(savedPayment);

            System.out.println("Payment request created: " + savedPayment);
            return savedPayment;

        } catch (Exception e) {
            System.err.println("Error creating payment request: " + e.getMessage());
            throw new RuntimeException("Failed to create payment request: " + e.getMessage());
        }
    }

    /**
     * Customer thanh toán payment request
     * @param payDTO Thông tin thanh toán
     * @return PaymentRequest đã được cập nhật
     */
    public PaymentRequest payPaymentRequest(PayPaymentRequestDTO payDTO) {
        try {
            // Lấy payment request
            Optional<PaymentRequest> paymentOpt = paymentRequestRepository.findById(payDTO.getPaymentId());
            if (!paymentOpt.isPresent()) {
                throw new RuntimeException("Payment request not found");
            }

            PaymentRequest paymentRequest = paymentOpt.get();

            // Kiểm tra trạng thái
            if (paymentRequest.getStatus() != PaymentRequest.PaymentStatus.PENDING) {
                throw new RuntimeException("Payment request is not pending");
            }

            // Kiểm tra account có khớp không
            if (!paymentRequest.getAccount().getAccountId().equals(payDTO.getAccountId())) {
                throw new RuntimeException("Account mismatch");
            }

            // Kiểm tra và trừ tiền từ số dư khả dụng
            Account account = paymentRequest.getAccount();
            Balance balance = balanceRepository.findByAccount(account)
                    .orElseThrow(() -> new RuntimeException("Balance not found"));

            java.math.BigDecimal available = balance.getAvailableBalance() != null
                    ? balance.getAvailableBalance()
                    : java.math.BigDecimal.ZERO;
            java.math.BigDecimal amount = java.math.BigDecimal.valueOf(paymentRequest.getAmount());

            if (available.compareTo(amount) < 0) {
                paymentRequest.setStatus(PaymentRequest.PaymentStatus.FAILED);
                paymentRequestRepository.save(paymentRequest);
                notificationService.sendPaymentFailureEmail(paymentRequest, "Insufficient available balance");
                throw new RuntimeException("Insufficient available balance");
            }

            // Trừ số dư khả dụng
            balance.setAvailableBalance(available.subtract(amount));
            balanceRepository.save(balance);

            // Thanh toán thành công
            paymentRequest.setStatus(PaymentRequest.PaymentStatus.PAID);
            paymentRequest.setPaidAt(LocalDateTime.now());
            PaymentRequest savedPayment = paymentRequestRepository.save(paymentRequest);

            // Gửi message đến queue để xử lý thông báo (không làm fail payment nếu JMS lỗi)
            try {
                jmsTemplate.convertAndSend(paymentQueue, savedPayment);
            } catch (Exception jmsError) {
                System.err.println("JMS send failed: " + jmsError.getMessage());
            }

            System.out.println("Payment completed: " + savedPayment);
            return savedPayment;

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách payment request của customer
     * @param accountId ID của account
     * @return List PaymentRequest
     */
    public List<PaymentRequest> getPaymentRequestsByAccount(Long accountId) {
        return paymentRequestRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    /**
     * Lấy danh sách payment request pending
     * @return List PaymentRequest
     */
    public List<PaymentRequest> getPendingPaymentRequests() {
        return paymentRequestRepository.findPendingPayments();
    }

    /**
     * Lấy danh sách tất cả payment request
     * @return List PaymentRequest
     */
    public List<PaymentRequest> getAllPaymentRequests() {
        return paymentRequestRepository.findAll();
    }
}
