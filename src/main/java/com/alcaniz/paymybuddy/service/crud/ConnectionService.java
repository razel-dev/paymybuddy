package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.connection.ConnectionDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ConnectionService {

    /**
     * Crée une connexion (owner -> related).
     * - vérifie l'existence des deux utilisateurs
     * - refuse les doublons
     * - refuse owner == related
     */
    ConnectionDTO create(@Valid UserConnectionDTO dto);

    /**
     * Liste des connexions d'un utilisateur (owner).
     */
    List<ConnectionDTO> getAllForOwner(Integer ownerUserId);

    /**
     * Supprime une connexion (idempotent).
     */
    void delete(Integer ownerUserId, Integer relatedUserId);

    /**
     * Vérifie l'existence d'une connexion.
     */
    boolean exists(Integer ownerUserId, Integer relatedUserId);
}