package com.alcaniz.paymybuddy.web.dto;

import java.time.Instant;

public record ConnectionDTO(
        Integer ownerUserId,
        Integer relatedUserId,
        Instant createdAt
) {}