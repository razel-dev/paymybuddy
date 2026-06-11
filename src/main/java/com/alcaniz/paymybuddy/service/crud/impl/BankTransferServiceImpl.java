package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.audit.FinancialOperationRecordedEvent;
import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.model.FinancialOperationSourceType;
import com.alcaniz.paymybuddy.model.FinancialOperationType;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.BankTransferRepository;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import com.alcaniz.paymybuddy.web.mapper.BankTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankTransferServiceImpl implements com.alcaniz.paymybuddy.service.crud.BankTransferService {

    private final BankTransferRepository bankTransferRepository;
    private final AccountRepository accountRepository;
    private final BankTransferMapper bankTransferMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    @Override
    public BankTransferDTO create(BankTransferCreateDTO dto) {
        log.debug("Appel de BankTransferService.create()");
        if (dto == null) {
            log.warn("create() refusee : DTO null");
            throw new BadRequestException("La requete de virement est vide.");
        }

        Integer accountId = dto.accountId();
        BigDecimal amount = dto.amount();
        BankTransferCreateDTO.BankTransferType type = dto.type();

        if (accountId == null) {
            throw new BadRequestException("L'identifiant du compte est obligatoire.");
        }
        if (amount == null || amount.compareTo(new BigDecimal("0.01")) < 0) {
            throw new BadRequestException("Le montant doit etre superieur ou egal a 0,01.");
        }
        if (type == null) {
            throw new BadRequestException("Le type de virement est obligatoire.");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BadRequestException("Compte introuvable: id=" + accountId));

        switch (type) {
            case DEPOSIT -> account.setBalance(account.getBalance().add(amount));
            case WITHDRAWAL -> {
                if (account.getBalance().compareTo(amount) < 0) {
                    log.warn("WITHDRAWAL refuse: solde insuffisant. Solde={}, montant={}", account.getBalance(), amount);
                    throw new BusinessException("Solde insuffisant pour effectuer le retrait.");
                }
                account.setBalance(account.getBalance().subtract(amount));
            }
        }

        BankTransfer toSave = bankTransferMapper.toEntity(dto);
        toSave.setAccount(account);

        accountRepository.save(account);
        BankTransfer saved = bankTransferRepository.save(toSave);
        applicationEventPublisher.publishEvent(new FinancialOperationRecordedEvent(
                mapOperationType(saved.getType()),
                FinancialOperationSourceType.BANK_TRANSFER,
                saved.getId(),
                account.getUser().getId(),
                account.getId(),
                null,
                saved.getAmount(),
                BigDecimal.ZERO,
                account.getCurrency(),
                saved.getType().name(),
                Instant.now()
        ));

        log.info("Virement cree id={} accountId={} type={} amount={}",
                saved.getId(), account.getId(), saved.getType(), saved.getAmount());
        return bankTransferMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BankTransferDTO> getHistoryForAccount(Integer accountId) {
        log.debug("Appel de getHistoryForAccount(accountId={})", accountId);
        if (accountId == null) {
            log.debug("getHistoryForAccount : accountId nul -> liste vide");
            return java.util.Collections.emptyList();
        }
        return bankTransferRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId)
                .stream()
                .map(bankTransferMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<BankTransferDTO> getById(Integer id) {
        log.debug("Appel de getById(id={})", id);
        if (id == null) {
            return java.util.Optional.empty();
        }
        return bankTransferRepository.findById(id).map(bankTransferMapper::toDto);
    }

    private FinancialOperationType mapOperationType(BankTransfer.TransferType type) {
        return switch (type) {
            case DEPOSIT -> FinancialOperationType.BANK_DEPOSIT;
            case WITHDRAWAL -> FinancialOperationType.BANK_WITHDRAWAL;
        };
    }
}
