package com.alcaniz.paymybuddy.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDTO(
        Integer id,
        Integer userId,
        String accountName,
        String currency,
        BigDecimal balance,
        Instant createdAt
) {}