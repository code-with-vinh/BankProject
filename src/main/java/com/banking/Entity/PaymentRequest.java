package com.banking.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.io.Serializable;

@Table(name = "payment_request")
@Entity
public class PaymentRequest implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonIgnore // tránh Jackson serialize nguyên object Account
    private Account account;

    @Column(name = "amount")
    private double amount;

    @Column(name = "currency")
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @Column(name = "paid_at")
    private java.time.LocalDateTime paidAt;

    public enum PaymentStatus {
        PENDING, PAID, FAILED, CANCELLED
    }

    public PaymentRequest() {}

    public PaymentRequest(double amount, Account account) {
        this.amount = amount;
        this.account = account;
        this.currency = "VND";
    }

    public PaymentRequest(double amount, Account account, String currency) {
        this.amount = amount;
        this.account = account;
        this.currency = currency;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    // ✅ Chỉ expose accountId ra JSON thay vì cả Account
    @JsonProperty("accountId")
    public Long getAccountId() {
        return account != null ? account.getAccountId() : null;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public java.time.LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(java.time.LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "paymentId=" + paymentId +
                ", accountId=" + getAccountId() +   // in ra id thay vì full object
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}