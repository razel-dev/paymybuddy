package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service

@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005"); // 0,5%

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO create(TransactionCreateDTO dto) {
        // 1) Vérifier/charger le compte émetteur
        var sender = accountRepository.findById(dto.senderAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Compte émetteur introuvable: " + dto.senderAccountId()));

        // 2) Résoudre l'email vers le compte destinataire (règle: compte par défaut du destinataire)
        var receiver = resolveReceiverAccountByEmail(dto.receiverEmail());

        // 3) Sécurité métier de base
        if (Objects.equals(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Le compte émetteur et le compte destinataire doivent être différents");
        }

        // 4) Mapper DTO -> entité puis hydrater les associations
        var entity = transactionMapper.toEntity(dto);
        entity.setSenderAccount(sender);
        entity.setReceiverAccount(receiver);

        // 5) Calcul des frais et mise à jour des soldes
        var amount = dto.amount().setScale(2, RoundingMode.HALF_UP);
        var fee = calculateFee(amount);
        var totalDebit = amount.add(fee);

        var senderBalance = sender.getBalance() == null ? BigDecimal.ZERO : sender.getBalance();
        if (senderBalance.compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer le virement");
        }

        // Débit émetteur (montant + frais) et crédit destinataire (montant)
        sender.setBalance(senderBalance.subtract(totalDebit));
        var receiverBalance = receiver.getBalance() == null ? BigDecimal.ZERO : receiver.getBalance();
        receiver.setBalance(receiverBalance.add(amount));

        // Persister les soldes (dirty checking suffirait, mais on peut expliciter)
        accountRepository.save(sender);
        accountRepository.save(receiver);

        // Renseigner les frais pour respecter la contrainte NOT NULL en base
        entity.setFee(fee);
        // Si nécessaire selon votre entité:
        // entity.setAmount(amount);

        // 6) Persister et retourner le DTO
        var saved = transactionRepository.save(entity);
        return transactionMapper.toDto(saved);
    }


    private Account resolveReceiverAccountByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email du destinataire est requis");
        }


        return accountRepository.findFirstByUser_EmailOrderByIdAsc(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte trouvé pour l'email: " + email));
    }
@Transactional(readOnly = true)
    @Override
    public List<TransactionDTO> getHistoryForAccount(Integer accountId) {
        log.debug("Appel de getHistoryForAccount(accountId={})", accountId);
        if (accountId == null) {
            log.debug("getHistoryForAccount : accountId nul -> liste vide");
            return Collections.emptyList();
        }
        return transactionRepository.findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
                accountId, accountId
        ).stream().map(transactionMapper::toDto).toList();
    }
@Transactional(readOnly = true)
    @Override
    public Optional<TransactionDTO> getById(Integer id) {
        log.debug("Appel de getById(id={})", id);
        if (id == null) return Optional.empty();
        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    // ---------- Règles de calcul ----------

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}