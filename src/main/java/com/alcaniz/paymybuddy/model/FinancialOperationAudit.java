package com.alcaniz.paymybuddy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Entity
@Immutable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "financial_operation_audit")
public class FinancialOperationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false, updatable = false, length = 32)
    private FinancialOperationType operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, updatable = false, length = 32)
    private FinancialOperationSourceType sourceType;

    @Column(name = "source_id", nullable = false, updatable = false)
    private Integer sourceId;

    @Column(name = "actor_user_id", nullable = false, updatable = false)
    private Integer actorUserId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Integer accountId;

    @Column(name = "counterparty_account_id", updatable = false)
    private Integer counterpartyAccountId;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal amount;

    @Column(name = "fee", nullable = false, precision = 10, scale = 2, updatable = false)
    private BigDecimal fee;

    @Column(name = "currency", nullable = false, length = 3, updatable = false, columnDefinition = "char(3)")
    private String currency;

    @Column(name = "description", length = 255, updatable = false)
    private String description;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    private FinancialOperationAudit(
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
        this.operationType = operationType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.actorUserId = actorUserId;
        this.accountId = accountId;
        this.counterpartyAccountId = counterpartyAccountId;
        this.amount = amount;
        this.fee = fee;
        this.currency = currency;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public static FinancialOperationAudit fromEvent(
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
        return new FinancialOperationAudit(
                operationType,
                sourceType,
                sourceId,
                actorUserId,
                accountId,
                counterpartyAccountId,
                amount,
                fee,
                currency,
                description,
                occurredAt
        );
    }
}
