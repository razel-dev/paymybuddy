package com.alcaniz.paymybuddy.service.reporting.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.web.dto.reporting.AccountReportDTO;
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
        //    On fixe la devise et le solde courant pour vérifier que le service les restitue tel quels.
        int accountId = 1;
        Account account = Account.builder()
                .accountName("acc-1")
                .currency("EUR")
                .balance(new BigDecimal("100.00"))
                .user(null) // l'utilisateur n'est pas pertinent pour ce scénario
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
        // 1) Métadonnées: le DTO doit reprendre l'ID de compte et la devise du compte trouvé.
        assertEquals(accountId, dto.accountId(), "Le DTO doit véhiculer l'ID du compte demandé");
        assertEquals("EUR", dto.currency(), "La devise doit provenir du compte récupéré");

        // 2) Agrégats: sans transactions, tout total (envoyé, reçu, frais) doit être à zéro.
        assertEquals(BigDecimal.ZERO, dto.totalSent(), "Total envoyé doit être 0 en absence de transactions");
        assertEquals(BigDecimal.ZERO, dto.totalReceived(), "Total reçu doit être 0 en absence de transactions");
        assertEquals(BigDecimal.ZERO, dto.totalFeesPaid(), "Total des frais doit être 0 en absence de transactions");

        // 3) Flux net: reçu - envoyé - frais = 0 - 0 - 0 = 0.
        assertEquals(BigDecimal.ZERO, dto.netFlow(), "Flux net doit être 0 sans mouvements");

        // 4) Solde courant: doit refléter le solde du compte, indépendant des agrégats calculés.
        assertEquals(new BigDecimal("100.00"), dto.currentBalance(), "Le solde courant doit être celui du compte");

        // 5) Bornes temporelles: comme on a passé null, le DTO doit restituer null.
        assertNull(dto.from(), "La borne 'from' doit être null si fournie null");
        assertNull(dto.to(), "La borne 'to' doit être null si fournie null");

        // 6) Interactions: vérifie que le service a interrogé les bons repositories et rien de plus.
        verify(accountRepository).findById(accountId);
        verify(transactionRepository).findAllBySenderAccount_IdOrReceiverAccount_IdOrderByCreatedAtDesc(accountId, accountId);
        verifyNoMoreInteractions(accountRepository, transactionRepository);
    }
}