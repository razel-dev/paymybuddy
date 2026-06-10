package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    UserConnectionRepository userConnectionRepository;

    @Mock
    TransactionMapper transactionMapper;

    @InjectMocks
    com.alcaniz.paymybuddy.service.crud.impl.TransactionServiceImpl service;

    private static Account acc(int accountId, int userId, String email, String balance, String currency) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        Account account = new Account();
        account.setUser(user);
        account.setAccountName("acc-" + accountId);
        account.setCurrency(currency);
        account.setBalance(new BigDecimal(balance));
        account.setId(accountId);
        return account;
    }

    private static Transaction baseTxForMapper(BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setSenderAccount(new Account());
        tx.setReceiverAccount(new Account());
        tx.setAmount(amount);
        tx.setFee(BigDecimal.ZERO);
        return tx;
    }

    @Test
    void create_rejetteDestinataireNonBuddy() {
        var dto = new TransactionCreateDTO(1, "mallory@example.com", new BigDecimal("25.00"), "x");
        var sender = acc(1, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "mallory@example.com", "10.00", "EUR");

        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("mallory@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2))).thenReturn(List.of(sender, receiver));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_verrouilleLesDeuxComptesDansOrdreCroissant() {
        var dto = new TransactionCreateDTO(5, "bob@example.com", new BigDecimal("10.00"), "ordered lock");
        var sender = acc(5, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");

        when(accountRepository.findById(5)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(5, 2))).thenReturn(List.of(receiver, sender));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount()));

        var saved = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(transactionMapper.toDto(saved)).thenReturn(mock(TransactionDTO.class));

        service.create(dto);

        verify(accountRepository).findAllByIdInOrderByIdAsc(List.of(5, 2));
    }

    @Test
    void create_soldeInsuffisant_lanceErreurEtPasDeSave() {
        var dto = new TransactionCreateDTO(1, "bob@example.com", new BigDecimal("50.00"), "x");
        var sender = acc(1, 101, "alice@example.com", "50.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "0.00", "EUR");

        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2))).thenReturn(List.of(sender, receiver));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount()));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
        assertEquals(new BigDecimal("50.00"), sender.getBalance());
        assertEquals(new BigDecimal("0.00"), receiver.getBalance());
    }

    @Test
    void create_happyPath_calculeFrais_metAJourSoldes_etMappeDto() {
        var dto = new TransactionCreateDTO(1, "bob@example.com", new BigDecimal("100.00"), "desc");
        var sender = acc(1, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");

        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2))).thenReturn(List.of(sender, receiver));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);

        Transaction base = baseTxForMapper(dto.amount());
        when(transactionMapper.toEntity(dto)).thenReturn(base);

        var saved = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        var dtoOut = mock(TransactionDTO.class);
        when(transactionMapper.toDto(saved)).thenReturn(dtoOut);

        var res = service.create(dto);

        assertSame(dtoOut, res);
        org.assertj.core.api.Assertions.assertThat(sender.getBalance()).isEqualByComparingTo("99.50");
        org.assertj.core.api.Assertions.assertThat(receiver.getBalance()).isEqualByComparingTo("110.00");

        verify(accountRepository, times(2)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toDto(saved);
    }

    @Test
    void getById_ok_mappeVersDto() {
        var entity = mock(Transaction.class);
        when(transactionRepository.findById(7)).thenReturn(Optional.of(entity));
        var dto = mock(TransactionDTO.class);
        when(transactionMapper.toDto(entity)).thenReturn(dto);

        var res = service.getById(7);

        assertTrue(res.isPresent());
        assertSame(dto, res.orElseThrow());
        verify(transactionRepository).findById(7);
        verify(transactionMapper).toDto(entity);
    }
}
