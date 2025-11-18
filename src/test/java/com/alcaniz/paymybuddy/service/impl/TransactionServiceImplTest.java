package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.service.crud.impl.TransactionServiceImpl;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
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
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock TransactionRepository txRepo;
    @Mock AccountRepository accRepo;
    @Mock TransactionMapper mapper;
    @InjectMocks
    TransactionServiceImpl service;

    // Helpers de création via builder (pas de new)
    private static Account accountWithBalance(int id, String balance) {
        User u = mock(User.class);
        Account a = Account.builder()
                .user(u)
                .accountName("acc-" + id)
                .currency("EUR")
                .balance(new BigDecimal(balance))
                .build();
        a.setId(id);
        return a;
    }

    private static Account dummyAccount() {
        User u = mock(User.class);
        return Account.builder()
                .user(u)
                .accountName("dummy")
                .currency("EUR")
                .balance(new BigDecimal("0.00"))
                .build();
    }

    @Test
    void create_ok_appliqueFraisEtSoldes() {
        // But : vérifier frais proportionnels (100 * 0,5% = 0,50),
        // soldes mis à jour (débit = montant + frais), et persistance.
        var dto = new TransactionCreateDTO(1, 2, new BigDecimal("100.00"), "t");
        var sender = accountWithBalance(1, "200.00");
        var receiver = accountWithBalance(2, "10.00");
        when(accRepo.findById(1)).thenReturn(Optional.of(sender));
        when(accRepo.findById(2)).thenReturn(Optional.of(receiver));

        // Le mapper retourne une entité "base" (peu importe les comptes/frais, ils sont réécrits par le service via toBuilder()).
        var mapped = Transaction.builder()
                .senderAccount(dummyAccount())
                .receiverAccount(dummyAccount())
                .description("t")
                .amount(dto.amount())
                .fee(BigDecimal.ZERO)
                .build();
        when(mapper.toEntity(dto)).thenReturn(mapped);
        when(txRepo.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        var res = service.create(dto);

        assertEquals(new BigDecimal("0.50"), res.getFee());
        assertEquals(new BigDecimal("99.50"), sender.getBalance());   // 200 - (100 + 0,50)
        assertEquals(new BigDecimal("110.00"), receiver.getBalance()); // 10 + 100
        verify(txRepo).save(any(Transaction.class));
    }

    @Test
    void create_nullDto() {
        // DTO null -> BadRequestException.
        assertThrows(BadRequestException.class, () -> service.create(null));
    }

    @Test
    void getHistory_accountNull_retourListeVide() {
        // accountId null -> liste vide, pas d'appel repository.
        assertTrue(service.getHistoryForAccount(null).isEmpty());
        verify(txRepo, never()).findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void getById_ok() {
        // Délégation repository quand l'id est fourni.
        var tx = Transaction.builder()
                .senderAccount(dummyAccount())
                .receiverAccount(dummyAccount())
                .amount(new BigDecimal("1.00"))
                .fee(BigDecimal.ZERO)
                .build();
        tx.setId(7);

        when(txRepo.findById(7)).thenReturn(Optional.of(tx));

        assertEquals(7, service.getById(7).orElseThrow().getId());
    }

    @Test
    void create_soldeInsuffisant() {
        // Solde émetteur ne couvre pas (montant + frais) -> BusinessException.
        var dto = new TransactionCreateDTO(1, 2, new BigDecimal("50.00"), null); // frais 0,25 -> total 50,25
        when(accRepo.findById(1)).thenReturn(Optional.of(accountWithBalance(1, "50.00")));
        when(accRepo.findById(2)).thenReturn(Optional.of(accountWithBalance(2, "0.00")));
        var mapped = Transaction.builder()
                .senderAccount(dummyAccount())
                .receiverAccount(dummyAccount())
                .amount(dto.amount())
                .fee(BigDecimal.ZERO)
                .build();
        when(mapper.toEntity(dto)).thenReturn(mapped);

        assertThrows(BusinessException.class, () -> service.create(dto));
        verify(txRepo, never()).save(any());
    }
}
