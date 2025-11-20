package com.alcaniz.paymybuddy.service.reporting.impl;

import com.alcaniz.paymybuddy.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionReportingServiceImplTest {

    @Mock
    TransactionRepository transactionRepository;

    @InjectMocks
    TransactionReportingServiceImpl service;

    // But: sans transactions et sans bornes temporelles -> totalFees = 0, from = null, to = null.
    @Test
    void getTotalPlatformFees_returnsZeroAndNullBounds() {
        // Arrange: le repository renvoie une liste vide (aucune transaction).
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act: calcul du total des frais sur toute la période (bornes nulles).
        var dto = service.getTotalPlatformFees(null, null);

        // Assert: totalFees = 0 et les bornes sont restituées telles quelles (null).
        assertEquals(BigDecimal.ZERO, dto.totalFees());
        assertEquals(null, dto.from());
        assertEquals(null, dto.to());
    }
}