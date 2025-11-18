package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    //  historique simple
    List<Transaction> findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
            Integer senderAccountId, Integer receiverAccountId
    );
}