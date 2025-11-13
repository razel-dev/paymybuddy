package com.alcaniz.paymybuddy.web.dto;

import java.time.Instant;

public record UserDTO(
        Integer id,
        String username,
        String email,
        Instant createdAt
) {}