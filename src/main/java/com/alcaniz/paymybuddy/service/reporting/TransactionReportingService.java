package com.alcaniz.paymybuddy.service.reporting;

import com.alcaniz.paymybuddy.web.dto.reporting.TotalFeesDTO;

import java.time.Instant;

public interface TransactionReportingService {

    /**
     * Total des frais plateforme sur la période demandée.
     * from/to optionnels (null = pas de borne).
     */
    TotalFeesDTO getTotalPlatformFees(Instant from, Instant to);
}