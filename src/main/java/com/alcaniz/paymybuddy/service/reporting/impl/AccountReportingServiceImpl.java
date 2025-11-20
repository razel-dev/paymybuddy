package com.alcaniz.paymybuddy.service.reporting.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.service.reporting.AccountReportingService;
import com.alcaniz.paymybuddy.web.dto.reporting.AccountReportDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AccountReportingServiceImpl implements AccountReportingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public AccountReportDTO getAccountReport(Integer accountId, Instant from, Instant to) {
        log.debug("AccountReportingService.getAccountReport(accountId={}, from={}, to={})", accountId, from, to);
        if (accountId == null) throw new BadRequestException("accountId est obligatoire.");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("Compte introuvable: id=" + accountId));

        Totals totals = transactionRepository
                .findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(accountId, accountId)
                .stream()
                .filter(t -> inPeriod(t.getCreatedAt(), from, to))
                .reduce(new Totals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                        (acc, t) -> {
                            // côté émetteur
                            if (t.getSenderAccount().getId().equals(accountId)) {
                                acc = acc.addSent(t.getAmount()).addFees(t.getFee());
                            }
                            // côté destinataire
                            if (t.getReceiverAccount().getId().equals(accountId)) {
                                acc = acc.addReceived(t.getAmount());
                            }
                            return acc;
                        },
                        Totals::merge);

        BigDecimal netFlow = totals.received().subtract(totals.sent()).subtract(totals.fees());

        return new AccountReportDTO(
                accountId,
                account.getCurrency(),
                totals.sent(),
                totals.received(),
                totals.fees(),
                netFlow,
                account.getBalance(),
                from,
                to
        );
    }

    private static boolean inPeriod(Instant createdAt, Instant from, Instant to) {
        return createdAt != null
                && (from == null || !createdAt.isBefore(from))
                && (to == null || !createdAt.isAfter(to));
    }

    private static record Totals(BigDecimal sent, BigDecimal fees, BigDecimal received) {
        Totals addSent(BigDecimal v) { return new Totals(sent.add(v), fees, received); }
        Totals addFees(BigDecimal v) { return new Totals(sent, fees.add(v), received); }
        Totals addReceived(BigDecimal v) { return new Totals(sent, fees, received.add(v)); }
        Totals merge(Totals o) { return new Totals(sent.add(o.sent), fees.add(o.fees), received.add(o.received)); }
    }
}