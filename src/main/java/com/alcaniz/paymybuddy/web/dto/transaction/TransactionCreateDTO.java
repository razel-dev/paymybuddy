package com.alcaniz.paymybuddy.web.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionCreateDTO(
        @NotNull
        @Positive
        Integer senderAccountId,

        @NotBlank
        @Email
        @Size(max = 255)
        String receiverEmail,

        @NotNull
        @DecimalMin("0.01")
        BigDecimal amount,

        @Size(max = 255)
        String description
) {

}