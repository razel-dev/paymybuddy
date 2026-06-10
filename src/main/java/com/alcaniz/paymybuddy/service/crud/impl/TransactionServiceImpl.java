package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
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

    private static final BigDecimal FEE_RATE = new BigDecimal("0.005");

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional
    public TransactionDTO create(TransactionCreateDTO dto) {
        var sender = accountRepository.findById(dto.senderAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Compte emetteur introuvable: " + dto.senderAccountId()));

        var receiver = resolveReceiverAccountByEmail(dto.receiverEmail());

        if (Objects.equals(sender.getId(), receiver.getId())) {
            throw new IllegalArgumentException("Le compte emetteur et le compte destinataire doivent etre differents");
        }

        if (!isAuthorizedConnection(sender, receiver)) {
            throw new IllegalArgumentException("Le destinataire doit etre une connexion autorisee");
        }

        var entity = transactionMapper.toEntity(dto);
        entity.setSenderAccount(sender);
        entity.setReceiverAccount(receiver);

        var amount = dto.amount().setScale(2, RoundingMode.HALF_UP);
        var fee = calculateFee(amount);
        var totalDebit = amount.add(fee);

        var senderBalance = sender.getBalance() == null ? BigDecimal.ZERO : sender.getBalance();
        if (senderBalance.compareTo(totalDebit) < 0) {
            throw new IllegalArgumentException("Solde insuffisant pour effectuer le virement");
        }

        sender.setBalance(senderBalance.subtract(totalDebit));
        var receiverBalance = receiver.getBalance() == null ? BigDecimal.ZERO : receiver.getBalance();
        receiver.setBalance(receiverBalance.add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        entity.setFee(fee);

        var saved = transactionRepository.save(entity);
        return transactionMapper.toDto(saved);
    }

    private Account resolveReceiverAccountByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email du destinataire est requis");
        }

        return accountRepository.findFirstByUser_EmailOrderByIdAsc(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun compte trouve pour l'email: " + email));
    }

    private boolean isAuthorizedConnection(Account sender, Account receiver) {
        if (sender.getUser() == null || sender.getUser().getId() == null) {
            return false;
        }
        if (receiver.getUser() == null || receiver.getUser().getId() == null) {
            return false;
        }
        return userConnectionRepository.existsByOwner_IdAndRelated_Id(
                sender.getUser().getId(),
                receiver.getUser().getId()
        );
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
        if (id == null) {
            return Optional.empty();
        }
        return transactionRepository.findById(id).map(transactionMapper::toDto);
    }

    private BigDecimal calculateFee(BigDecimal amount) {
        return amount.multiply(FEE_RATE).setScale(2, RoundingMode.HALF_UP);
    }
}
