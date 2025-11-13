package com.alcaniz.paymybuddy.web.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record BankTransferCreateDTO(
        @NotNull
        @Positive
        Integer accountId,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @NotNull
        BankTransferType type
) {
    // Aligné avec l’ENUM('DEPOSIT','WITHDRAWAL') SQL
    public enum BankTransferType { DEPOSIT, WITHDRAWAL }


    @AssertTrue(message = "Le montant doit être strictement positif")
    public boolean isPositiveAmount() {
        return amount != null && amount.compareTo(new BigDecimal("0.00")) > 0;
    }
}