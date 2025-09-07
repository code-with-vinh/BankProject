package com.banking.Service;

import com.banking.Entity.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Email Service
 * 
 * Handles all email-related operations including payment notifications,
 * confirmation emails, and system notifications.
 * Uses Thymeleaf templates for HTML email generation.
 * 
 * @author Banking System Team
 * @version 1.0
 * @since 2024
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.admin}")
    private String adminEmail;

    /**
     * Gửi email thông báo thanh toán thành công
     */
    public void sendPaymentConfirmationEmail(PaymentRequest paymentRequest) {
        try {
            String customerEmail = paymentRequest.getAccount().getEmail();
            String subject = "Xác nhận thanh toán thành công - Banking System";
            
            // Tạo nội dung email HTML
            String htmlContent = createPaymentConfirmationHtml(paymentRequest);
            
            sendHtmlEmail(customerEmail, subject, htmlContent);
            
            System.out.println("📧 Payment confirmation email sent to: " + customerEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment confirmation email: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo thanh toán thất bại
     */
    public void sendPaymentFailureEmail(PaymentRequest paymentRequest, String reason) {
        try {
            String customerEmail = paymentRequest.getAccount().getEmail();
            String subject = "Thông báo thanh toán thất bại - Banking System";
            
            String htmlContent = createPaymentFailureHtml(paymentRequest, reason);
            
            sendHtmlEmail(customerEmail, subject, htmlContent);
            
            System.out.println("📧 Payment failure email sent to: " + customerEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending payment failure email: " + e.getMessage());
        }
    }

    /**
     * Gửi email thông báo cho admin
     */
    public void sendAdminNotificationEmail(PaymentRequest paymentRequest, String status) {
        try {
            String subject = "Thông báo thanh toán mới - Banking System Admin";
            
            String htmlContent = createAdminNotificationHtml(paymentRequest, status);
            
            sendHtmlEmail(adminEmail, subject, htmlContent);
            
            System.out.println("📧 Admin notification email sent to: " + adminEmail);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending admin notification email: " + e.getMessage());
        }
    }

    /**
     * Gửi email HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    /**
     * Gửi email payment request
     */
    public void sendPaymentRequestEmail(String to, String subject, String htmlContent) {
        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Error sending payment request email: " + e.getMessage());
        }
    }

    /**
     * Gửi email payment failure
     */
    public void sendPaymentFailureEmail(String to, String subject, String htmlContent) {
        try {
            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Error sending payment failure email: " + e.getMessage());
        }
    }

    /**
     * Tạo HTML cho email xác nhận thanh toán
     */
    private String createPaymentConfirmationHtml(PaymentRequest paymentRequest) {
        Context context = new Context();
        context.setVariable("paymentId", paymentRequest.getPaymentId());
        context.setVariable("accountId", paymentRequest.getAccount().getAccountId());
        context.setVariable("customerName", paymentRequest.getAccount().getCustomerName());
        context.setVariable("amount", formatCurrency(paymentRequest.getAmount(), paymentRequest.getCurrency()));
        context.setVariable("currency", paymentRequest.getCurrency());
        context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        return templateEngine.process("email/payment-confirmation", context);
    }

    /**
     * Tạo HTML cho email thanh toán thất bại
     */
    private String createPaymentFailureHtml(PaymentRequest paymentRequest, String reason) {
        Context context = new Context();
        context.setVariable("paymentId", paymentRequest.getPaymentId());
        context.setVariable("accountId", paymentRequest.getAccount().getAccountId());
        context.setVariable("customerName", paymentRequest.getAccount().getCustomerName());
        context.setVariable("amount", formatCurrency(paymentRequest.getAmount(), paymentRequest.getCurrency()));
        context.setVariable("currency", paymentRequest.getCurrency());
        context.setVariable("reason", reason);
        context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        return templateEngine.process("email/payment-failure", context);
    }

    /**
     * Tạo HTML cho email thông báo admin
     */
    private String createAdminNotificationHtml(PaymentRequest paymentRequest, String status) {
        Context context = new Context();
        context.setVariable("paymentId", paymentRequest.getPaymentId());
        context.setVariable("accountId", paymentRequest.getAccount().getAccountId());
        context.setVariable("customerName", paymentRequest.getAccount().getCustomerName());
        context.setVariable("customerEmail", paymentRequest.getAccount().getEmail());
        context.setVariable("amount", formatCurrency(paymentRequest.getAmount(), paymentRequest.getCurrency()));
        context.setVariable("currency", paymentRequest.getCurrency());
        context.setVariable("status", status);
        context.setVariable("date", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        
        return templateEngine.process("email/admin-notification", context);
    }

    /**
     * Format tiền tệ
     */
    private String formatCurrency(double amount, String currency) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.US);
        return formatter.format(amount) + " " + currency;
    }

    /**
     * Gửi email đơn giản (fallback)
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            
            System.out.println("📧 Simple email sent to: " + to);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending simple email: " + e.getMessage());
        }
    }
}
