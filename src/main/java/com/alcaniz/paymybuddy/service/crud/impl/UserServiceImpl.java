package com.alcaniz.paymybuddy.service.crud.impl;

import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User create(@Valid UserCreateDTO dto) {
        log.debug("Appel de create()");
        if (dto == null) {
            log.warn("create() refusée : DTO null");
            throw new BadRequestException("La requête de création d'utilisateur est vide.");
        }

        String rawUsername = dto.username();
        String rawEmail = dto.email();
        String rawPassword = dto.password();

        if (!StringUtils.hasText(rawUsername)) {
            log.warn("create() refusée : nom d'utilisateur vide");
            throw new BadRequestException("Le nom d'utilisateur est obligatoire.");
        }
        if (!StringUtils.hasText(rawEmail)) {
            log.warn("create() refusée : email vide");
            throw new BadRequestException("L'email est obligatoire.");
        }
        if (!StringUtils.hasText(rawPassword)) {
            log.warn("create() refusée : mot de passe vide");
            throw new BadRequestException("Le mot de passe est obligatoire.");
        }

        String username = rawUsername.trim();
        String email = rawEmail.trim().toLowerCase();

        if (!isEmailAvailable(email)) {
            log.warn("create() refusée : email déjà utilisé [{}]", email);
            throw new BusinessException("L'email est déjà utilisé.");
        }

        String passwordHash = passwordEncoder.encode(rawPassword);

        // Construction via Lombok builder (évite le new User() qui est inaccessible)
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordHash)
                .build();

        User saved = userRepository.save(user);
        log.info("Utilisateur créé avec id={} email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getById(Integer id) {
        log.debug("Appel de getById(id={})", id);
        if (id == null) {
            log.debug("getById : id nul -> Optional.empty()");
            return Optional.empty();
        }
        Optional<User> res = userRepository.findById(id);
        log.debug("getById : utilisateur trouvé = {}", res.isPresent());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getByEmail(String email) {
        log.debug("Appel de getByEmail(email={})", email);
        if (!StringUtils.hasText(email)) {
            log.debug("getByEmail : email vide -> Optional.empty()");
            return Optional.empty();
        }
        Optional<User> res = userRepository.findByEmail(email.trim().toLowerCase());
        log.debug("getByEmail : utilisateur trouvé = {}", res.isPresent());
        return res;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        log.debug("Appel de isEmailAvailable(email={})", email);
        if (!StringUtils.hasText(email)) {
            log.debug("isEmailAvailable : email vide -> false");
            return false;
        }
        boolean disponible = !userRepository.existsByEmail(email.trim().toLowerCase());
        log.debug("isEmailAvailable : disponible = {}", disponible);
        return disponible;
    }

    @Override
    public void deleteById(Integer id) {
        log.debug("Appel de deleteById(id={})", id);
        if (id == null) {
            log.debug("deleteById : id nul -> aucune action (idempotent)");
            return; // idempotent
        }
        if (!userRepository.existsById(id)) {
            log.debug("deleteById : utilisateur {} introuvable -> aucune action (idempotent)", id);
            return; // idempotent
        }
        userRepository.deleteById(id);
        log.info("Utilisateur supprimé id={}", id);
    }
}