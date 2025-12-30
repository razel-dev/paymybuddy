package com.alcaniz.paymybuddy.service.crud;


import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
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
    TransactionDTO create(@Valid TransactionCreateDTO dto);

    /**
     * Récupère l'historique (émises ou reçues) d'un compte donné, du plus récent au plus ancien.
     */
    List<TransactionDTO> getHistoryForAccount(Integer accountId);

    /**
     * Récupère une transaction par son id.
     */
    Optional<TransactionDTO> getById(Integer id);


}