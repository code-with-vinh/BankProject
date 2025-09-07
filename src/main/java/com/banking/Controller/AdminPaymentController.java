package com.banking.Controller;

import com.banking.DTO.CreatePaymentRequestDTO;
import com.banking.Entity.PaymentRequest;
import com.banking.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/payment")
public class AdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Trang tạo payment request cho customer
     */
    @GetMapping("/create-request")
    public String createPaymentRequestPage(Model model) {
        model.addAttribute("createDTO", new CreatePaymentRequestDTO());
        return "admin/createPaymentRequest";
    }

    /**
     * Xử lý tạo payment request
     */
    @PostMapping("/create-request")
    @ResponseBody
    public String createPaymentRequest(@RequestBody CreatePaymentRequestDTO createDTO) {
        try {
            PaymentRequest paymentRequest = paymentService.createPaymentRequest(createDTO);
            return "Payment request created successfully! ID: " + paymentRequest.getPaymentId();
        } catch (Exception e) {
            return "Error creating payment request: " + e.getMessage();
        }
    }

    /**
     * Trang xem tất cả payment requests
     */
    @GetMapping("/all-requests")
    public String viewAllPaymentRequests(Model model) {
        List<PaymentRequest> paymentRequests = paymentService.getAllPaymentRequests();
        model.addAttribute("paymentRequests", paymentRequests);
        return "admin/allPaymentRequests";
    }

//    /**
//     * Trang xem payment requests pending
//     */
//    @GetMapping("/pending-requests")
//    public String viewPendingPaymentRequests(Model model) {
//        List<PaymentRequest> pendingRequests = paymentService.getPendingPaymentRequests();
//        model.addAttribute("paymentRequests", pendingRequests);
//        return "admin/pendingPaymentRequests";
//    }

    /**
     * API lấy danh sách payment requests
     */
    @GetMapping("/api/all")
    @ResponseBody
    public List<PaymentRequest> getAllPaymentRequests() {
        return paymentService.getAllPaymentRequests();
    }

    /**
     * API lấy danh sách payment requests pending
     */
    @GetMapping("/api/pending")
    @ResponseBody
    public List<PaymentRequest> getPendingPaymentRequests() {
        return paymentService.getPendingPaymentRequests();
    }
}
