package com.banking.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name ="account_transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;
    
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    
    @Column(name = "amount")
    private BigDecimal amount;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "card_send")
    private String cardSend;
    
    @Column(name = "card_receipt")
    private String cardReceiptNumber;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id")
    private Account account;

    public Transaction() {}

    public Transaction(LocalDate transactionDate, BigDecimal amount, String type, String status, String cardSend, String cardReceiptNumber, Account account) {
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.cardSend = cardSend;
        this.cardReceiptNumber = cardReceiptNumber;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCardSend() {
        return cardSend;
    }

    public void setCardSend(String cardSend) {
        this.cardSend = cardSend;
    }

    public String getCardReceiptNumber() {
        return cardReceiptNumber;
    }

    public void setCardReceiptNumber(String cardReceiptNumber) {
        this.cardReceiptNumber = cardReceiptNumber;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", transactionDate=" + transactionDate +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", cardSend='" + cardSend + '\'' +
                ", cardReceiptNumber='" + cardReceiptNumber + '\'' +
                '}';
    }
}
