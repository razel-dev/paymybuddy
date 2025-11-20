package com.alcaniz.paymybuddy.service.reporting.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountReportingServiceImplTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;

    @InjectMocks
    AccountReportingServiceImpl service;

    @Test
    void getAccountReport_noTransactions_returnsZerosAndAccountInfo() {
        // Arrange
        // 1) Préparer un compte existant retourné par le repository.
        //    Le builder impose user/accountName/currency non null -> on mocke un User pour éviter la NPE.
        int accountId = 1;
        Account account = Account.builder()
                .user(mock(com.alcaniz.paymybuddy.model.User.class)) // évite la NPE: @NonNull sur 'user'
                .accountName("acc-1")
                .currency("EUR")
                .balance(new BigDecimal("100.00"))
                .build();
        account.setId(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // 2) Simuler l'absence de transactions pour cet account (liste vide).
        when(transactionRepository.findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(accountId, accountId))
                .thenReturn(Collections.emptyList());

        // Act
        // Appeler la méthode à tester avec des bornes temporelles nulles (toute période).
        AccountReportDTO dto = service.getAccountReport(accountId, null, null);

        // Assert
        // 1) Métadonnées: l'ID et la devise du compte doivent être recopiés dans le DTO.
        assertEquals(accountId, dto.accountId(), "Le DTO doit véhiculer l'ID du compte demandé");
        assertEquals("EUR", dto.currency(), "La devise doit provenir du compte récupéré");

        // 2) Agrégats: sans transactions, les totaux (envoyé, reçu, frais) doivent être à zéro.
        assertEquals(BigDecimal.ZERO, dto.totalSent(), "Total envoyé doit être 0 en absence de transactions");
        assertEquals(BigDecimal.ZERO, dto.totalReceived(), "Total reçu doit être 0 en absence de transactions");
        assertEquals(BigDecimal.ZERO, dto.totalFeesPaid(), "Total des frais doit être 0 en absence de transactions");

        // 3) Flux net: reçu - envoyé - frais = 0 - 0 - 0 = 0.
        assertEquals(BigDecimal.ZERO, dto.netFlow(), "Flux net doit être 0 sans mouvements");

        // 4) Solde courant: doit refléter le solde du compte, indépendant des agrégats calculés.
        assertEquals(new BigDecimal("100.00"), dto.currentBalance(), "Le solde courant doit être celui du compte");

        // 5) Bornes temporelles: null en entrée -> null en sortie.
        assertNull(dto.from(), "La borne 'from' doit être null si fournie null");
        assertNull(dto.to(), "La borne 'to' doit être null si fournie null");

        // 6) Interactions: vérifie que seules les dépendances nécessaires ont été appelées.
        verify(accountRepository).findById(accountId);
        verify(transactionRepository).findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(accountId, accountId);
        verifyNoMoreInteractions(accountRepository, transactionRepository);
    }
}