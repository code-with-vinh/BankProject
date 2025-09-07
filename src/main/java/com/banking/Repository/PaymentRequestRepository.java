package com.banking.Repository;

import com.banking.Entity.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {
    
    List<PaymentRequest> findByAccount_AccountId(Long accountId);
    
    List<PaymentRequest> findByStatus(PaymentRequest.PaymentStatus status);
    
    @Query("SELECT p FROM PaymentRequest p WHERE p.account.accountId = :accountId AND p.status = :status")
    List<PaymentRequest> findByAccountIdAndStatus(@Param("accountId") Long accountId, @Param("status") PaymentRequest.PaymentStatus status);
    
    @Query("SELECT p FROM PaymentRequest p WHERE p.status = 'PENDING' ORDER BY p.createdAt DESC")
    List<PaymentRequest> findPendingPayments();
    
    @Query("SELECT p FROM PaymentRequest p WHERE p.account.accountId = :accountId ORDER BY p.createdAt DESC")
    List<PaymentRequest> findByAccountIdOrderByCreatedAtDesc(@Param("accountId") Long accountId);
}
