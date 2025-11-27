package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.BankTransfer;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.BankTransferRepository;
import com.alcaniz.paymybuddy.service.crud.impl.BankTransferServiceImpl;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.mapper.BankTransferMapper;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// création (DEPOSIT), historique (null -> vide), getById (ok).
@ExtendWith(MockitoExtension.class)
class BankTransferServiceImplTest {

    @Mock BankTransferRepository bankTransferRepository;
    @Mock AccountRepository accountRepository;
    @Mock BankTransferMapper bankTransferMapper;

    @InjectMocks
    BankTransferServiceImpl service;

    private static Account accountWithBalance(int id, String balance) {
        User u = mock(User.class);
        Account a = new Account();
        a.setUser(u);
        a.setAccountName("acc-" + id);
        a.setCurrency("EUR");
        a.setBalance(new BigDecimal(balance));
        a.setId(id);
        return a;
    }

    private static Account dummyAccount() {
        User u = mock(User.class);
        Account a = new Account();
        a.setUser(u);
        a.setAccountName("dummy");
        a.setCurrency("EUR");
        a.setBalance(new BigDecimal("0.00"));
        return a;
    }

    @Test
    void create() {
        //  DEPOSIT -> solde += amount, virement persistant.
        var dto = new BankTransferCreateDTO(1, new BigDecimal("50.00"), BankTransferCreateDTO.BankTransferType.DEPOSIT);
        var acc = accountWithBalance(1, "10.00");
        when(accountRepository.findById(1)).thenReturn(Optional.of(acc));

        var mapped = new BankTransfer();
        mapped.setAccount(dummyAccount());
        mapped.setAmount(dto.amount());
        mapped.setType(BankTransfer.TransferType.DEPOSIT);
        when(bankTransferMapper.toEntity(dto)).thenReturn(mapped);
        when(bankTransferRepository.save(any(BankTransfer.class))).thenAnswer(i -> i.getArgument(0));

        when(bankTransferMapper.toDto(any(BankTransfer.class))).thenAnswer(i -> {
            BankTransfer bt = i.getArgument(0);
            return new com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO(
                    bt.getId(), bt.getAccount().getId(), bt.getAmount(),
                    com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO.BankTransferType.valueOf(bt.getType().name()),
                    bt.getCreatedAt()
            );
        });

        var res = service.create(dto);

        assertEquals(new BigDecimal("60.00"), acc.getBalance()); // 10 + 50
        assertEquals(new BigDecimal("50.00"), res.amount());
        verify(accountRepository).save(acc);
        verify(bankTransferRepository).save(any(BankTransfer.class));
        verify(bankTransferMapper).toDto(any(BankTransfer.class));
    }

    @Test
    void getHistoryForAccount() {
        // accountId null -> liste vide, pas d'appel repository.
        assertTrue(service.getHistoryForAccount(null).isEmpty());
        verify(bankTransferRepository, never()).findAllByAccount_IdOrderByCreatedAtDesc(any());
        // appel rapide
        when(bankTransferRepository.findAllByAccount_IdOrderByCreatedAtDesc(3)).thenReturn(Collections.emptyList());
        assertNotNull(service.getHistoryForAccount(3));
        verify(bankTransferRepository).findAllByAccount_IdOrderByCreatedAtDesc(3);
    }

    @Test
    void getById() {
        // Chemin OK: délégation au repository.
        var bt = new BankTransfer();
        bt.setAccount(dummyAccount());
        bt.setAmount(new BigDecimal("1.00"));
        bt.setType(BankTransfer.TransferType.DEPOSIT);
        when(bankTransferRepository.findById(9)).thenReturn(Optional.of(bt));
        // Stub du mapping en DTO pour que l'Optional contienne bien une valeur
        when(bankTransferMapper.toDto(bt)).thenReturn(
                new com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO(
                        9, null, new BigDecimal("1.00"),
                        com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferDTO.BankTransferType.DEPOSIT,
                        null
                )
        );

        assertEquals(9, service.getById(9).orElseThrow().id());
        verify(bankTransferMapper).toDto(bt);
    }
}