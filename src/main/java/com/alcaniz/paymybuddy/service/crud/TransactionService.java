package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
public interface TransactionService {

    /**
     * Crée une transaction entre deux comptes:
     * - vérifie l'existence des comptes
     * - applique des frais
     * - met à jour les soldes
     * - enregistre la transaction
     */
    Transaction create(@Valid TransactionCreateDTO dto);

    /**
     * Récupère l'historique (émises ou reçues) d'un compte donné, du plus récent au plus ancien.
     */
    List<Transaction> getHistoryForAccount(Integer accountId);

    /**
     * Récupère une transaction par son id.
     */
    Optional<Transaction> getById(Integer id);

    @Validated
    interface BankTransferService {

        /**
         * Crée un virement bancaire (DEPOSIT ou WITHDRAWAL) pour un compte:
         * - vérifie l'existence du compte
         * - vérifie le montant
         * - applique la logique de solde (ajout ou retrait)
         * - enregistre le virement
         */
        BankTransfer create(@Valid BankTransferCreateDTO dto);

        /**
         * Historique des virements d'un compte (plus récents d'abord).
         */
        List<BankTransfer> getHistoryForAccount(Integer accountId);

        /**
         * Récupère un virement par id.
         */
        Optional<BankTransfer> getById(Integer id);
    }
}