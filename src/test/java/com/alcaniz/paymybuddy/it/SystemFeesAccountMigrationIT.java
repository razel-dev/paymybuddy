package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
class SystemFeesAccountMigrationIT {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void startup_should_create_system_fees_account() {
        var systemAccount = accountRepository.findFirstByUser_EmailOrderByIdAsc("system@paymybuddy.local")
                .orElseThrow();

        assertThat(systemAccount.getAccountName()).isEqualTo("PayMyBuddy Fees");
        assertThat(systemAccount.getCurrency()).isEqualTo("EUR");
        assertThat(systemAccount.getBalance()).isEqualByComparingTo("0.00");
    }
}
