package com.alcaniz.paymybuddy.web.dto.connection;

import com.alcaniz.paymybuddy.web.dto.user.UserDTO;

import java.time.Instant;

public record ConnectionDTO(
        Integer ownerUserId,
        Integer relatedUserId,
        Instant createdAt,
        UserDTO relatedUser
) {}