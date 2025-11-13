package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.BankTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankTransferRepository extends JpaRepository<BankTransfer, Integer> {
    // Optionnel V1: historique par compte
    List<BankTransfer> findAllByAccount_IdOrderByCreatedAtDesc(Integer accountId);

}