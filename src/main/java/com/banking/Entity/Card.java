package com.banking.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
@Table(name = "Card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;

    @Column(name = "card_number")
    private String cardNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "card_type", nullable = false, length = 50)
    private String cardType;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;


    public Card() {}

    public Card(Account account, String cardType, LocalDate expiryDate, String status) {
        this.account = account;
        this.cardType = cardType;
        this.expiryDate = expiryDate;
        this.status = status;
    }


    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", cardType='" + cardType + '\'' +
                ", expiryDate=" + expiryDate +
                ", status='" + status + '\'' +
                '}';
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }
}
