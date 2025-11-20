package com.alcaniz.paymybuddy.service.reporting.impl;

import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.service.reporting.TransactionReportingService;
import com.alcaniz.paymybuddy.web.dto.reporting.TotalFeesDTO;
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
public class TransactionReportingServiceImpl implements TransactionReportingService {

    private final TransactionRepository transactionRepository;

    @Override
    public TotalFeesDTO getTotalPlatformFees(Instant from, Instant to) {
        log.debug("TransactionReportingService.getTotalPlatformFees(from={}, to={})", from, to);
        BigDecimal total = transactionRepository.findAll().stream()
                .filter(t -> inPeriod(t.getCreatedAt(), from, to))
                .map(Transaction::getFee)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
        return new TotalFeesDTO(total, from, to);
    }

    private static boolean inPeriod(Instant createdAt, Instant from, Instant to) {
        return createdAt != null
                && (from == null || !createdAt.isBefore(from))
                && (to == null || !createdAt.isAfter(to));
    }
}