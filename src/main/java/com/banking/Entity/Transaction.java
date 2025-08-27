package com.banking.Entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name ="account_transaction")
public class Transaction {
    @Id
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


}
