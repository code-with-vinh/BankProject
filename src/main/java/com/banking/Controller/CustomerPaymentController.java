package com.banking.Controller;

import com.banking.DTO.PayPaymentRequestDTO;
import com.banking.Entity.Account;
import com.banking.Entity.PaymentRequest;
import com.banking.Service.AccountService;
import com.banking.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer/payment")
public class CustomerPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AccountService accountService;

    /**
     * Trang xem payment requests của customer
     */
    @GetMapping("/my-requests")
    public String viewMyPaymentRequests( Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email;
        Object principal = authentication.getPrincipal();
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername();
        } else {
            email = principal.toString();
        }
        Account acc = accountService.findByEmail(email);
        if (acc == null) {
            return "redirect:/auth/login";
        }


        List<PaymentRequest> paymentRequests = paymentService.getPaymentRequestsByAccount(acc.getAccountId());
        model.addAttribute("paymentRequests", paymentRequests);
        model.addAttribute("accountId", acc.getAccountId());
        return "customer/myPaymentRequests";
    }

    /**
     * Trang thanh toán payment request
     */
    @GetMapping("/pay/{paymentId}")
    public String payPaymentRequestPage(@PathVariable Long paymentId, Model model) {
        PaymentRequest paymentRequest = paymentService.getPaymentById(paymentId);
        if (paymentRequest == null) {
            model.addAttribute("error", "Payment request not found");
            return "customer/payPaymentRequest";
        }

        Long accountId = paymentRequest.getAccountId();
        model.addAttribute("paymentRequest", paymentRequest);
        model.addAttribute("accountId", accountId);
        model.addAttribute("payDTO", new PayPaymentRequestDTO(paymentId, accountId));
        return "customer/payPaymentRequest";
    }

    /**
     * Xử lý thanh toán payment request
     */
    @PostMapping("/pay")
    @ResponseBody
    public String payPaymentRequest(@RequestBody PayPaymentRequestDTO payDTO) {
        try {
            PaymentRequest paymentRequest = paymentService.payPaymentRequest(payDTO);
            return "Payment completed successfully! Status: " + paymentRequest.getStatus();
        } catch (Exception e) {
            return "Error processing payment: " + e.getMessage();
        }
    }

    /**
     * API lấy payment requests của customer
     */
    @GetMapping("/api/my-requests")
    @ResponseBody
    public List<PaymentRequest> getMyPaymentRequests(@RequestParam Long accountId) {
        return paymentService.getPaymentRequestsByAccount(accountId);
    }

    /**
     * API thanh toán payment request
     */
    @PostMapping("/api/pay")
    @ResponseBody
    public PaymentRequest payPaymentRequestApi(@RequestBody PayPaymentRequestDTO payDTO) {
        return paymentService.payPaymentRequest(payDTO);
    }

}
