package com.alcaniz.paymybuddy.web.dto.reporting;

import java.math.BigDecimal;
import java.time.Instant;

// DTO d'exposition (web) pour le total des frais plateforme
public record TotalFeesDTO(
        BigDecimal totalFees,
        Instant from,
        Instant to
) {}