package com.banking.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Balance")
public class Balance {
    @Id
    @Column(name = "account_id")
    private Long accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "available_balance", nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Column(name = "hold_balance", nullable = false)
    private BigDecimal holdBalance = BigDecimal.ZERO;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getHoldBalance() {
        return holdBalance;
    }

    public void setHoldBalance(BigDecimal holdBalance) {
        this.holdBalance = holdBalance;
    }

    @Override
    public String toString() {
        return "Balance{" +
                ", accountId=" + (account != null ? account.getAccountId() : null) +
                ", availableBalance=" + availableBalance +
                ", holdBalance=" + holdBalance +
                '}';
    }
}
