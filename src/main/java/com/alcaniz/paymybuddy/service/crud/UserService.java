package com.alcaniz.paymybuddy.service.crud;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User create(UserCreateDTO dto);

    Optional<User> getById(Integer id);

    Optional<User> getByEmail(String email);

    boolean isEmailAvailable(String email);

    void deleteById(Integer id);

    @Validated
    interface AccountService {

        /**
         * Crée un compte pour un utilisateur donné:
         * - vérifie l'existence de l'utilisateur
         * - normalise les données (trim/uppercase)
         * - initialise le solde (0.00 côté DB)
         * - persiste le compte
         */
        Account create(@Valid AccountCreateDTO dto);

        /**
         * Récupère un compte par son id.
         */
        Optional<Account> getById(Integer id);

        /**
         * Récupère tous les comptes d’un utilisateur.
         */
        List<Account> getAllForUser(Integer userId);

        /**
         * Supprime un compte par son id (idempotent).
         */
        void deleteById(Integer id);
    }
}