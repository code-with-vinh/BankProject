package com.banking.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayPaymentRequestDTO {
    
    @JsonProperty("paymentId")
    private Long paymentId;
    
    @JsonProperty("accountId")
    private Long accountId;
    
    public PayPaymentRequestDTO() {
    }
    
    public PayPaymentRequestDTO(Long paymentId, Long accountId) {
        this.paymentId = paymentId;
        this.accountId = accountId;
    }
    
    public Long getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    @Override
    public String toString() {
        return "PayPaymentRequestDTO{" +
                "paymentId=" + paymentId +
                ", accountId=" + accountId +
                '}';
    }
}
