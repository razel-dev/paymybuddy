package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import com.alcaniz.paymybuddy.web.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionMapper transactionMapper;

    @InjectMocks
    com.alcaniz.paymybuddy.service.crud.impl.TransactionServiceImpl service;

    // ---------- Helpers ----------
    private static Account acc(int id, String bal, String cur) {
        User u = new User();
        Account a = new Account();
        a.setUser(u);
        a.setAccountName("acc-" + id);
        a.setCurrency(cur);
        a.setBalance(new BigDecimal(bal));
        a.setId(id);
        return a;
    }

    // Base de Transaction pour le mapper, sans builder (utilise les setters)
    private static Transaction baseTxForMapper(BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setSenderAccount(new Account());
        tx.setReceiverAccount(new Account());
        tx.setAmount(amount);
        tx.setFee(BigDecimal.ZERO);
        return tx;
    }

    @Test
    void create_soldeInsuffisant_lanceBusiness_et_pasDeSave() {
        // Arrange
        var dto = new TransactionCreateDTO(1, 2, new BigDecimal("50.00"), "x"); // total débit = 50,25
        var sender = acc(1, "50.00", "EUR");
        var receiver = acc(2, "0.00", "EUR");

        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(2)).thenReturn(Optional.of(receiver));
        // Surtout ne pas stubber le mapper ici : le service jette avant de l'atteindre

        // Act + Assert
        assertThrows(BusinessException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
        assertEquals(new BigDecimal("50.00"), sender.getBalance());
        assertEquals(new BigDecimal("0.00"), receiver.getBalance());
    }

    @Test
    void create_happyPath_calculeFrais_metAJourSoldes_etMappeDto() {
        // Arrange
        var dto = new TransactionCreateDTO(1, 2, new BigDecimal("100.00"), "desc");
        var sender = acc(1, "200.00", "EUR");
        var receiver = acc(2, "10.00", "EUR");

        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findById(2)).thenReturn(Optional.of(receiver));

        // Important: toEntity retourne une base "builder-safe"
        Transaction base = baseTxForMapper(dto.amount());
        when(transactionMapper.toEntity(dto)).thenReturn(base);

        var saved = new Transaction(); // on peut garder simple
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        var dtoOut = mock(TransactionDTO.class);
        when(transactionMapper.toDto(saved)).thenReturn(dtoOut);

        // Act
        var res = service.create(dto);

        // Assert
        assertSame(dtoOut, res);
        // 100.00 + 0.5% = 0.50 -> 200 - 100.50 = 99.50 ; 10 + 100 = 110
        org.assertj.core.api.Assertions.assertThat(sender.getBalance()).isEqualByComparingTo("99.50");
        org.assertj.core.api.Assertions.assertThat(receiver.getBalance()).isEqualByComparingTo("110.00");

        verify(accountRepository, times(2)).save(any(Account.class)); // sender + receiver
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toDto(saved);
    }

    @Test
    void getById_ok_mappeVersDto() {
        // Arrange
        var entity = mock(Transaction.class);
        when(transactionRepository.findById(7)).thenReturn(Optional.of(entity));
        var dto = mock(TransactionDTO.class);
        when(transactionMapper.toDto(entity)).thenReturn(dto);

        // Act
        var res = service.getById(7);

        // Assert
        assertTrue(res.isPresent());
        assertSame(dto, res.orElseThrow());
        verify(transactionRepository).findById(7);
        verify(transactionMapper).toDto(entity);
    }
}