package com.alcaniz.paymybuddy.web.dto.banktransfer;

import java.math.BigDecimal;
import java.time.Instant;

public record BankTransferDTO(
        Integer id,
        Integer accountId,
        BigDecimal amount,
        BankTransferType type,
        Instant createdAt
) {
    public enum BankTransferType { DEPOSIT, WITHDRAWAL }
}