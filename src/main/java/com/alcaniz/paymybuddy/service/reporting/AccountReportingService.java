package com.alcaniz.paymybuddy.service.reporting;

import com.alcaniz.paymybuddy.web.dto.reporting.AccountReportDTO;

import java.time.Instant;

public interface AccountReportingService {

    /**
     * Rapport simple basé sur les transactions d’un compte:
     * - total envoyé, total reçu, frais payés
     * - netFlow = reçus − envoyés − frais
     * - solde courant
     * Les paramètres from/to sont optionnels (null = pas de borne).
     */
    AccountReportDTO getAccountReport(Integer accountId, Instant from, Instant to);
}