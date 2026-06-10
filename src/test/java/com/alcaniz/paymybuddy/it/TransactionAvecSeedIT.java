package com.alcaniz.paymybuddy.it;
import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;

import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.context.annotation.Import;

@ActiveProfiles("test")

@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
@Transactional
class TransactionAvecSeedIT {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;

    // test similaire qu'avec seed mais le but et de verifier la santé des migrations
    @Test
    void transfert_depuis_seed_met_a_jour_les_soldes() {
        // 1) Arrange: récupérer les comptes
        var aliceAccount = accountRepository.findById(1000).orElseThrow(); // Alice  à 100.00
        var bobAccount   = accountRepository.findById(1001).orElseThrow(); // Bob  à 50.00

        // 2) Act: exécuter un transfert de 10.00 (frais 0,5% = 0,05 si ta règle est proportionnelle)
        var dto = new TransactionCreateDTO(aliceAccount.getId(), bobAccount.getUser().getEmail(), new BigDecimal("10.00"), "seed run");
        var result = transactionService.create(dto);

        // 3) Assert: vérifier frais et nouveaux soldes
        assertThat(result.fee()).isEqualByComparingTo("0.05");

        var refreshedAlice = accountRepository.findById(aliceAccount.getId()).orElseThrow();
        var refreshedBob   = accountRepository.findById(bobAccount.getId()).orElseThrow();

        // Soldes attendus selon la règle de frais. Exemple avec 0,5% :
        // Alice: 100.00 - (10.00 + 0.05) = 89.95
        // Bob:   50.00  + 10.00 = 60.00
        assertThat(refreshedAlice.getBalance()).isEqualByComparingTo("89.95");
        assertThat(refreshedBob.getBalance()).isEqualByComparingTo("60.00");
    }
}
