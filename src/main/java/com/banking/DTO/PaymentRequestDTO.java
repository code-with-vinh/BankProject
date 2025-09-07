package com.banking.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentRequestDTO {
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("currency")
    private String currency = "VND";
    
    @JsonProperty("accountId")
    private Long accountId;
    
    public PaymentRequestDTO() {
    }
    
    public PaymentRequestDTO(double amount, String currency, Long accountId) {
        this.amount = amount;
        this.currency = currency;
        this.accountId = accountId;
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
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    @Override
    public String toString() {
        return "PaymentRequestDTO{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", accountId=" + accountId +
                '}';
    }
}
