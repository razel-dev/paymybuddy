package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;

import com.alcaniz.paymybuddy.model.UserConnectionId;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.ConnectionService;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.connection.ConnectionDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import com.alcaniz.paymybuddy.web.mapper.UserConnectionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service

@RequiredArgsConstructor
@Slf4j
public class ConnectionServiceImpl implements ConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final UserRepository userRepository;
    private final UserConnectionMapper userConnectionMapper;

    @Transactional
    @Override
    public ConnectionDTO create(UserConnectionDTO dto) {
        log.debug("Appel de ConnectionService.create()");
        if (dto == null) {
            log.warn("create() refusée : DTO null");
            throw new BadRequestException("La requête de création de connexion est vide.");
        }

        Integer ownerId = dto.ownerUserId();
        Integer relatedId = dto.relatedUserId();

        if (ownerId == null || relatedId == null) {
            throw new BadRequestException("Les identifiants ownerUserId et relatedUserId sont obligatoires.");
        }
        if (ownerId.equals(relatedId)) {
            throw new BusinessException("Impossible de se connecter à soi-même.");
        }

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new BadRequestException("Utilisateur owner introuvable: id=" + ownerId));
        User related = userRepository.findById(relatedId)
                .orElseThrow(() -> new BadRequestException("Utilisateur related introuvable: id=" + relatedId));

        if (userConnectionRepository.existsByOwner_IdAndRelated_Id(ownerId, relatedId)) {
            throw new BusinessException("La connexion existe déjà.");
        }

        UserConnection toSave = userConnectionMapper.toEntity(dto);
        toSave.setOwner(owner);
        toSave.setRelated(related);
        toSave.setId(new UserConnectionId(ownerId, relatedId));
        UserConnection saved = userConnectionRepository.save(toSave);
        log.info("Connexion créée ownerId={} relatedId={}", ownerId, relatedId);
        return userConnectionMapper.toDto(saved);
    }
    @Transactional(readOnly = true)
    @Override
    public List<ConnectionDTO> getAllForOwner(Integer ownerUserId) {
        log.debug("Appel de getAllForOwner(ownerUserId={})", ownerUserId);
        if (ownerUserId == null) {
            log.debug("getAllForOwner : ownerUserId nul -> liste vide");
            return java.util.Collections.emptyList();
        }
        return userConnectionRepository.findAllByOwner_Id(ownerUserId).stream()
                .map(userConnectionMapper::toDto)
                .toList();
    }
    @Transactional
    @Override
    public void delete(Integer ownerUserId, Integer relatedUserId) {
        log.debug("Appel de delete(ownerUserId={}, relatedUserId={})", ownerUserId, relatedUserId);
        if (ownerUserId == null || relatedUserId == null) {
            log.debug("delete : ids nuls -> aucune action (idempotent)");
            return;
        }
        if (!userConnectionRepository.existsByOwner_IdAndRelated_Id(ownerUserId, relatedUserId)) {
            log.debug("delete : connexion introuvable -> aucune action (idempotent)");
            return;
        }
        userConnectionRepository.deleteByOwner_IdAndRelated_Id(ownerUserId, relatedUserId);
        log.info("Connexion supprimée ownerId={} relatedId={}", ownerUserId, relatedUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Integer ownerUserId, Integer relatedUserId) {
        log.debug("Appel de exists(ownerUserId={}, relatedUserId={})", ownerUserId, relatedUserId);
        if (ownerUserId == null || relatedUserId == null) return false;
        return userConnectionRepository.existsByOwner_IdAndRelated_Id(ownerUserId, relatedUserId);
    }
}