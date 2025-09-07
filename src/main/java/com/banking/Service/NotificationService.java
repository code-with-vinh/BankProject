package com.banking.Service;

import com.banking.Entity.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    /**
     * Lắng nghe message từ payment queue và xử lý thông báo
     * @param paymentRequest Thông tin thanh toán từ queue
     */
    // @JmsListener(destination = "payment.queue") // Tạm thời disable để test
    public void handlePaymentNotification(PaymentRequest paymentRequest) {
        try {
            System.out.println("=== PAYMENT NOTIFICATION ===");
            System.out.println("Payment confirmed for paymentId: " + paymentRequest.getPaymentId());
            System.out.println("Account ID: " + paymentRequest.getAccount().getAccountId());
            System.out.println("Amount: " + paymentRequest.getAmount() + " " + paymentRequest.getCurrency());
            System.out.println("=============================");
            
            // Gửi email thông báo thành công
            emailService.sendPaymentConfirmationEmail(paymentRequest);
            
            // Gửi email thông báo cho admin
            emailService.sendAdminNotificationEmail(paymentRequest, "SUCCESS");
            
            // Giả lập gửi SMS
            sendSmsNotification(paymentRequest);
            
        } catch (Exception e) {
            System.err.println("Error processing payment notification: " + e.getMessage());
            
            // Gửi email thông báo lỗi
            try {
                emailService.sendPaymentFailureEmail(paymentRequest, "System error: " + e.getMessage());
                emailService.sendAdminNotificationEmail(paymentRequest, "ERROR");
            } catch (Exception emailError) {
                System.err.println("Error sending failure email: " + emailError.getMessage());
            }
        }
    }

    /**
     * Xử lý thanh toán thất bại
     * @param paymentRequest Thông tin thanh toán
     * @param reason Lý do thất bại
     */
    public void handlePaymentFailure(PaymentRequest paymentRequest, String reason) {
        try {
            System.out.println("=== PAYMENT FAILURE ===");
            System.out.println("Payment failed for paymentId: " + paymentRequest.getPaymentId());
            System.out.println("Reason: " + reason);
            System.out.println("=======================");
            
            // Gửi email thông báo thất bại
            emailService.sendPaymentFailureEmail(paymentRequest, reason);
            
            // Gửi email thông báo cho admin
            emailService.sendAdminNotificationEmail(paymentRequest, "FAILED");
            
            // Giả lập gửi SMS thất bại
            sendSmsFailureNotification(paymentRequest, reason);
            
        } catch (Exception e) {
            System.err.println("Error processing payment failure notification: " + e.getMessage());
        }
    }

    /**
     * Giả lập gửi SMS thông báo thành công
     * @param paymentRequest Thông tin thanh toán
     */
    private void sendSmsNotification(PaymentRequest paymentRequest) {
        System.out.println("📱 SMS SENT:");
        System.out.println("   To: +84901234567");
        System.out.println("   Message: Payment of " + paymentRequest.getAmount() + 
                          " " + paymentRequest.getCurrency() + " confirmed. ID: " + 
                          paymentRequest.getPaymentId());
    }

    /**
     * Giả lập gửi SMS thông báo thất bại
     * @param paymentRequest Thông tin thanh toán
     * @param reason Lý do thất bại
     */
    private void sendSmsFailureNotification(PaymentRequest paymentRequest, String reason) {
        System.out.println("📱 SMS SENT:");
        System.out.println("   To: +84901234567");
        System.out.println("   Message: Payment of " + paymentRequest.getAmount() + 
                          " " + paymentRequest.getCurrency() + " failed. Reason: " + reason);
    }

    /**
     * Gửi email thông báo payment request mới cho customer
     */
    public void sendPaymentRequestNotification(PaymentRequest paymentRequest) {
        try {
            String customerEmail = paymentRequest.getAccount().getEmail();
            String subject = "Yêu cầu thanh toán mới - Banking System";
            
            String htmlContent = createPaymentRequestHtml(paymentRequest);
            
            emailService.sendPaymentRequestEmail(customerEmail, subject, htmlContent);
            
            System.out.println("📧 Payment request notification sent to: " + customerEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment request notification: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo thanh toán thất bại
     */
    public void sendPaymentFailureEmail(PaymentRequest paymentRequest, String reason) {
        try {
            String customerEmail = paymentRequest.getAccount().getEmail();
            String subject = "Thanh toán thất bại - Banking System";
            
            String htmlContent = createPaymentFailureHtml(paymentRequest, reason);
            
            emailService.sendPaymentFailureEmail(customerEmail, subject, htmlContent);
            
            System.out.println("📧 Payment failure email sent to: " + customerEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment failure email: " + e.getMessage());
        }
    }

    /**
     * Tạo HTML cho email payment request
     */
    private String createPaymentRequestHtml(PaymentRequest paymentRequest) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Payment Request</title></head>" +
                "<body style='font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #333;'>💳 Yêu cầu thanh toán mới</h2>" +
                "<p>Xin chào " + paymentRequest.getAccount().getCustomerName() + ",</p>" +
                "<p>Bạn có một yêu cầu thanh toán mới cần xử lý:</p>" +
                "<div style='background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0;'>" +
                "<p><strong>Số tiền:</strong> " + String.format("%,.0f", paymentRequest.getAmount()) + " " + paymentRequest.getCurrency() + "</p>" +
                "<p><strong>Mô tả:</strong> " + (paymentRequest.getDescription() != null ? paymentRequest.getDescription() : "Không có mô tả") + "</p>" +
                "<p><strong>Ngày tạo:</strong> " + paymentRequest.getCreatedAt().toString() + "</p>" +
                "</div>" +
                "<p>Vui lòng đăng nhập vào hệ thống để thanh toán.</p>" +
                "<p>Trân trọng,<br>Banking System</p>" +
                "</div></body></html>";
    }

    /**
     * Tạo HTML cho email payment failure
     */
    private String createPaymentFailureHtml(PaymentRequest paymentRequest, String reason) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'><title>Payment Failed</title></head>" +
                "<body style='font-family: Arial, sans-serif; background: #f4f4f4; padding: 20px;'>" +
                "<div style='max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px;'>" +
                "<h2 style='color: #dc3545;'>❌ Thanh toán thất bại</h2>" +
                "<p>Xin chào " + paymentRequest.getAccount().getCustomerName() + ",</p>" +
                "<p>Thanh toán của bạn đã thất bại:</p>" +
                "<div style='background: #f8d7da; padding: 20px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #dc3545;'>" +
                "<p><strong>Số tiền:</strong> " + String.format("%,.0f", paymentRequest.getAmount()) + " " + paymentRequest.getCurrency() + "</p>" +
                "<p><strong>Lý do:</strong> " + reason + "</p>" +
                "<p><strong>Ngày tạo:</strong> " + paymentRequest.getCreatedAt().toString() + "</p>" +
                "</div>" +
                "<p>Vui lòng kiểm tra lại thông tin và thử lại.</p>" +
                "<p>Trân trọng,<br>Banking System</p>" +
                "</div></body></html>";
    }
}
