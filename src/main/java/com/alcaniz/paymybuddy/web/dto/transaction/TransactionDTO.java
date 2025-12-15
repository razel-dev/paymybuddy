package com.alcaniz.paymybuddy.web.dto.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionDTO(
        Integer id,
        Integer senderAccountId,
        Integer receiverAccountId,
        String receiverEmail,
        String description,
        BigDecimal amount,
        BigDecimal fee,
        Instant createdAt
) {}