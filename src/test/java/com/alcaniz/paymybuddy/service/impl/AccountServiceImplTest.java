package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.impl.AccountServiceImpl;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.mapper.AccountMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock UserRepository userRepository;
    @Mock AccountMapper accountMapper;

    @InjectMocks
    AccountServiceImpl service;

    @Test
    void create() {
        // But: créer un compte avec normalisation et persistance (heureux chemin).
        var dto = new AccountCreateDTO(1, "  Mon Compte  ", "eur");
        User user = mock(User.class);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        Account mapped = new Account();
        mapped.setUser(user);
        mapped.setAccountName("Mon Compte");
        mapped.setCurrency("EUR");
        when(accountMapper.toEntity(any(AccountCreateDTO.class))).thenReturn(mapped);

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(42);
            return a;
        });
        // On renvoie un DTO non null pour éviter un NPE dans map()
        var dtoResult = mock(com.alcaniz.paymybuddy.web.dto.account.AccountDTO.class);
        when(accountMapper.toDto(any(Account.class))).thenReturn(dtoResult);

        service.create(dto);

        // On vérifie les interactions clefs (existence user, mapping, save, mapping retour)
        verify(userRepository).findById(1);
        verify(accountMapper).toEntity(any(AccountCreateDTO.class));
        verify(accountRepository).save(any(Account.class));
        verify(accountMapper).toDto(any(Account.class));
    }

    @Test
    void getById() {
        // But: null -> Optional.empty() sans accès repo ; sinon délégation au repo.
        assertTrue(service.getById(null).isEmpty());
        verify(accountRepository, never()).findById(any());

        Account acc = new Account();
        acc.setUser(mock(User.class));
        acc.setAccountName("A");
        acc.setCurrency("EUR");
        acc.setId(7);
        when(accountRepository.findById(7)).thenReturn(Optional.of(acc));
        // On renvoie un DTO non null pour éviter un NPE dans Optional.map(...)
        var dtoMapped = mock(com.alcaniz.paymybuddy.web.dto.account.AccountDTO.class);
        when(accountMapper.toDto(acc)).thenReturn(dtoMapped);

        assertTrue(service.getById(7).isPresent());
        verify(accountRepository).findById(7);
        verify(accountMapper).toDto(acc);
    }

    @Test
    void getAllForUser() {
        // But: null -> liste vide sans repo ; sinon délégation au repo.
        assertTrue(service.getAllForUser(null).isEmpty());
        verify(accountRepository, never()).findAllByUser_Id(any());

        when(accountRepository.findAllByUser_Id(5)).thenReturn(Collections.emptyList());
        assertNotNull(service.getAllForUser(5));
        verify(accountRepository).findAllByUser_Id(5);
    }

    @Test
    void deleteById() {
        // But: si existe -> deleteById appelé ; sinon idempotent (pas de delete).
        when(accountRepository.existsById(10)).thenReturn(true);
        service.deleteById(10);
        verify(accountRepository).deleteById(10);

        when(accountRepository.existsById(11)).thenReturn(false);
        service.deleteById(11);
        verify(accountRepository, never()).deleteById(11);

        service.deleteById(null);
        verify(accountRepository, atMostOnce()).deleteById(10); // seulement le premier delete
    }
}