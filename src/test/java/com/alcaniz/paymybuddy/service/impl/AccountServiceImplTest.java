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

        Account mapped = Account.builder()
                .user(user)
                .accountName("Mon Compte")
                .currency("EUR")
                .build();
        when(accountMapper.toEntity(any(AccountCreateDTO.class))).thenReturn(mapped);

        Account saved = mapped.toBuilder().build();
        saved.setId(42);
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account res = service.create(dto);

        assertEquals(42, res.getId());
        verify(userRepository).findById(1);
        verify(accountMapper).toEntity(any(AccountCreateDTO.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void getById() {
        // But: null -> Optional.empty() sans accès repo ; sinon délégation au repo.
        assertTrue(service.getById(null).isEmpty());
        verify(accountRepository, never()).findById(any());

        Account acc = Account.builder()
                .user(mock(User.class))
                .accountName("A")
                .currency("EUR")
                .build();
        acc.setId(7);
        when(accountRepository.findById(7)).thenReturn(Optional.of(acc));

        assertEquals(7, service.getById(7).orElseThrow().getId());
        verify(accountRepository).findById(7);
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