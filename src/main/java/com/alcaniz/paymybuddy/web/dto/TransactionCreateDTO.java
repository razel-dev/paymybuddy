package com.alcaniz.paymybuddy.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionCreateDTO(
        @NotNull
        @Positive
        Integer senderAccountId,

        @NotNull
        @Positive
        Integer receiverAccountId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,


        @Size(max = 255)
        String description
) {
    // Contrainte logique: comptes différents (équivalent du CHECK différentiel attendu côté métier)
    @AssertTrue(message = "senderAccountId et receiverAccountId doivent être différents")
    public boolean isDistinctAccounts() {
        return senderAccountId != null
                && receiverAccountId != null
                && !senderAccountId.equals(receiverAccountId);
    }
}