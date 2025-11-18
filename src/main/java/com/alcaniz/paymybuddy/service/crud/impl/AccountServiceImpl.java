package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements UserService.AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Override
    public Account create(AccountCreateDTO dto) {
        log.debug("Appel de AccountService.create()");
        if (dto == null) {
            log.warn("create() refusée : DTO null");
            throw new BadRequestException("La requête de création de compte est vide.");
        }

        Integer userId = dto.userId();
        String rawName = dto.accountName();
        String rawCurrency = dto.currency();

        if (userId == null) {
            throw new BadRequestException("L'identifiant utilisateur est obligatoire.");
        }
        if (!StringUtils.hasText(rawName)) {
            throw new BadRequestException("Le nom du compte est obligatoire.");
        }
        if (!StringUtils.hasText(rawCurrency)) {
            throw new BadRequestException("La devise est obligatoire.");
        }

        // Existence de l’utilisateur (FK)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Utilisateur introuvable: id=" + userId));

        // Normalisations
        String name = rawName.trim();
        String currency = rawCurrency.trim().toUpperCase(); // DTO impose déjà [A-Z]{3}, on normalise par robustesse

        // MapStruct: crée l'entité avec user.id depuis le DTO; on remplace par l'entité user chargée
        Account toSave = accountMapper.toEntity(new AccountCreateDTO(userId, name, currency));
        toSave.setUser(user);

        Account saved = accountRepository.save(toSave);
        log.info("Compte créé id={} userId={} name={} currency={}", saved.getId(), userId, saved.getAccountName(), saved.getCurrency());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> getById(Integer id) {
        log.debug("Appel de AccountService.getById(id={})", id);
        if (id == null) {
            log.debug("getById : id nul -> Optional.empty()");
            return Optional.empty();
        }
        return accountRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> getAllForUser(Integer userId) {
        log.debug("Appel de AccountService.getAllForUser(userId={})", userId);
        if (userId == null) {
            log.debug("getAllForUser : userId nul -> liste vide");
            return Collections.emptyList();
        }
        return accountRepository.findAllByUser_Id(userId);
    }

    @Override
    public void deleteById(Integer id) {
        log.debug("Appel de AccountService.deleteById(id={})", id);
        if (id == null) {
            log.debug("deleteById : id nul -> aucune action (idempotent)");
            return;
        }
        if (!accountRepository.existsById(id)) {
            log.debug("deleteById : compte {} introuvable -> aucune action (idempotent)", id);
            return;
        }
        accountRepository.deleteById(id);
        log.info("Compte supprimé id={}", id);
    }
}