package com.banking.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePaymentRequestDTO {
    
    @JsonProperty("accountId")
    private Long accountId;
    
    @JsonProperty("amount")
    private double amount;
    
    @JsonProperty("currency")
    private String currency = "VND";
    
    @JsonProperty("description")
    private String description;
    
    public CreatePaymentRequestDTO() {
    }
    
    public CreatePaymentRequestDTO(Long accountId, double amount, String currency, String description) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "CreatePaymentRequestDTO{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
