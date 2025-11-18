package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.BankTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, Integer> {
    //  historique par compte
    List<BankTransfer> findAllByAccount_IdOrderByCreatedAtDesc(Integer accountId);

}