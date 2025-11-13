package com.alcaniz.paymybuddy.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AccountCreateDTO(
        @NotNull
        @Positive
        Integer userId,

        @NotBlank
        @Size(max = 100)
        String accountName,

        // ISO 4217 sur 3 lettres, en majuscules. (CHAR(3) en base)
        @NotBlank
        @Pattern(regexp = "^[A-Z]{3}$")
        String currency
) {}