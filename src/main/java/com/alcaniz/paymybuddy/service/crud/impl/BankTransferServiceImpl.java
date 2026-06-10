package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.BankTransferRepository;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import com.alcaniz.paymybuddy.web.mapper.BankTransferMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service

@RequiredArgsConstructor
@Slf4j
public class BankTransferServiceImpl implements com.alcaniz.paymybuddy.service.crud.BankTransferService {

    private final BankTransferRepository bankTransferRepository;
    private final AccountRepository accountRepository;
    private final BankTransferMapper bankTransferMapper;

    @Transactional
    @Override
    public BankTransferDTO create(BankTransferCreateDTO dto) {
        log.debug("Appel de BankTransferService.create()");
        if (dto == null) {
            log.warn("create() refusée : DTO null");
            throw new BadRequestException("La requête de virement est vide.");
        }
        Integer accountId = dto.accountId();
        java.math.BigDecimal amount = dto.amount();
        BankTransferCreateDTO.BankTransferType type = dto.type();

        if (accountId == null) {
            throw new BadRequestException("L'identifiant du compte est obligatoire.");
        }
        if (amount == null || amount.compareTo(new java.math.BigDecimal("0.01")) < 0) {
            throw new BadRequestException("Le montant doit être supérieur ou égal à 0,01.");
        }
        if (type == null) {
            throw new BadRequestException("Le type de virement est obligatoire.");
        }

        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new BadRequestException("Compte introuvable: id=" + accountId));

        // Application de la logique métier sur le solde
        switch (type) {
            case DEPOSIT -> account.setBalance(account.getBalance().add(amount));
            case WITHDRAWAL -> {
                if (account.getBalance().compareTo(amount) < 0) {
                    log.warn("WITHDRAWAL refusé: solde insuffisant. Solde={}, montant={}", account.getBalance(), amount);
                    throw new BusinessException("Solde insuffisant pour effectuer le retrait.");
                }
                account.setBalance(account.getBalance().subtract(amount));
            }
        }

        // Construction et persistance du virement
        BankTransfer toSave = bankTransferMapper.toEntity(dto);
        toSave.setAccount(account); // remplace l'id mappé par l'entité chargée

        accountRepository.save(account);
        BankTransfer saved = bankTransferRepository.save(toSave);

        log.info("Virement créé id={} accountId={} type={} amount={}",
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
        if (id == null) return java.util.Optional.empty();
        return bankTransferRepository.findById(id).map(bankTransferMapper::toDto);
    }
}
