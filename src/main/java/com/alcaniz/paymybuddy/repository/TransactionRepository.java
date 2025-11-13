package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Java
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    //  historique simple
    List<Transaction> findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
            Integer senderAccountId, Integer receiverAccountId
    );
}