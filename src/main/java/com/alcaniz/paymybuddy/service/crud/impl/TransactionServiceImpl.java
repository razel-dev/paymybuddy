package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.mapper.TransactionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005"); // 0,5%


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Transactional(readOnly = true)
    @Override
    public Transaction create(TransactionCreateDTO dto) {
        log.debug("Appel de TransactionService.create()");
        if (dto == null) {
            log.warn("create() refusée : DTO null");
            throw new BadRequestException("La requête de création de transaction est vide.");
        }

        Integer senderId = dto.senderAccountId();
        Integer receiverId = dto.receiverAccountId();
        BigDecimal amount = dto.amount();

        // Rappels de validations défensives (en plus des annotations)
        if (senderId == null || receiverId == null) {
            throw new BadRequestException("Les identifiants des comptes émetteur et destinataire sont obligatoires.");
        }
        if (senderId.equals(receiverId)) {
            throw new BusinessException("Les comptes émetteur et destinataire doivent être différents.");
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new BadRequestException("Le montant doit être supérieur ou égal à 0,01.");
        }

        // Chargement des comptes
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new BadRequestException("Compte émetteur introuvable: id=" + senderId));
        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new BadRequestException("Compte destinataire introuvable: id=" + receiverId));

        // Règle métier : mêmes devises uniquement (si applicable)
        if (StringUtils.hasText(sender.getCurrency()) && StringUtils.hasText(receiver.getCurrency())
                && !sender.getCurrency().equals(receiver.getCurrency())) {
            throw new BusinessException("Les transferts ne sont autorisés qu'entre comptes de même devise.");
        }

        // Calcul des frais
        BigDecimal fee = calculateFee(amount);

        // Vérification de solde suffisant (montant + frais)
        BigDecimal totalDebit = amount.add(fee);
        if (sender.getBalance().compareTo(totalDebit) < 0) {
            log.warn("create() refusée : solde insuffisant. Solde={}, Débit requis={}", sender.getBalance(), totalDebit);
            throw new BusinessException("Solde insuffisant pour effectuer cette transaction.");
        }

        // Mouvement de fonds
        sender.setBalance(sender.getBalance().subtract(totalDebit));
        receiver.setBalance(receiver.getBalance().add(amount));

        // Construction et persistance de la transaction
        Transaction toSave = transactionMapper.toEntity(dto)
                .toBuilder()
                .senderAccount(sender)
                .receiverAccount(receiver)
                .fee(fee)
                .build();

        // Sauvegardes atomiques (dans la même transaction)
        accountRepository.save(sender);
        accountRepository.save(receiver);
        Transaction saved = transactionRepository.save(toSave);

        log.info("Transaction créée id={} montant={} frais={} sender={} receiver={}",
                saved.getId(), saved.getAmount(), saved.getFee(), sender.getId(), receiver.getId());
        return saved;
    }

    @Override

    public List<Transaction> getHistoryForAccount(Integer accountId) {
        log.debug("Appel de getHistoryForAccount(accountId={})", accountId);
        if (accountId == null) {
            log.debug("getHistoryForAccount : accountId nul -> liste vide");
            return Collections.emptyList();
        }
        return transactionRepository.findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(
                accountId, accountId
        );
    }

    @Override

    public Optional<Transaction> getById(Integer id) {
        log.debug("Appel de getById(id={})", id);
        if (id == null) return Optional.empty();
        return transactionRepository.findById(id);
    }

    // ---------- Règles de calcul ----------

    private BigDecimal calculateFee(BigDecimal amount) {
        // Frais = montant * rate, arrondi à 2 décimales (HALF_UP), sans minimum
        return amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}