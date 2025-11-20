package com.alcaniz.paymybuddy.web.dto.reporting;

import java.math.BigDecimal;
import java.time.Instant;

// DTO d'exposition (web) pour le rapport de compte
public record AccountReportDTO(
        Integer accountId,
        String currency,
        BigDecimal totalSent,
        BigDecimal totalReceived,
        BigDecimal totalFeesPaid,
        BigDecimal netFlow,
        BigDecimal currentBalance,
        Instant from,
        Instant to
) {}