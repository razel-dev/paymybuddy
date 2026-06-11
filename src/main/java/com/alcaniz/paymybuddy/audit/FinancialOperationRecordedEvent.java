package com.alcaniz.paymybuddy.audit;

import com.alcaniz.paymybuddy.model.FinancialOperationSourceType;
import com.alcaniz.paymybuddy.model.FinancialOperationType;

import java.math.BigDecimal;
import java.time.Instant;

public record FinancialOperationRecordedEvent(
        FinancialOperationType operationType,
        FinancialOperationSourceType sourceType,
        Integer sourceId,
        Integer actorUserId,
        Integer accountId,
        Integer counterpartyAccountId,
        BigDecimal amount,
        BigDecimal fee,
        String currency,
        String description,
        Instant occurredAt
) {
}
