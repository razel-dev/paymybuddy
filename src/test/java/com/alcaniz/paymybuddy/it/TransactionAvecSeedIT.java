package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
@Transactional
class TransactionAvecSeedIT {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionService transactionService;

    @Test
    void transfert_depuis_seed_credite_les_frais_sur_le_compte_systeme() {
        var aliceAccount = accountRepository.findById(1000).orElseThrow();
        var bobAccount = accountRepository.findById(1001).orElseThrow();

        var dto = new TransactionCreateDTO(
                aliceAccount.getId(),
                bobAccount.getUser().getEmail(),
                new BigDecimal("10.00"),
                "seed run"
        );
        var result = transactionService.create(dto);

        assertThat(result.fee()).isEqualByComparingTo("0.05");

        var refreshedAlice = accountRepository.findById(aliceAccount.getId()).orElseThrow();
        var refreshedBob = accountRepository.findById(bobAccount.getId()).orElseThrow();
        var systemFeesAccount = accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(
                "system@paymybuddy.local",
                "PayMyBuddy Fees"
        ).orElseThrow();

        assertThat(refreshedAlice.getBalance()).isEqualByComparingTo("89.95");
        assertThat(refreshedBob.getBalance()).isEqualByComparingTo("60.00");
        assertThat(systemFeesAccount.getBalance()).isEqualByComparingTo("0.05");
    }
}
