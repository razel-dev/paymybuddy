package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.model.UserConnectionId;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
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
class TransactionSansSeedIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserConnectionRepository userConnectionRepository;

    @Autowired
    private TransactionService transactionService;

    @Test
    void transfert_conserve_la_somme_des_soldes_en_creditant_le_compte_systeme() {
        var alice = new User();
        alice.setUsername("alice-it");
        alice.setEmail("alice-it@test.local");
        alice.setPassword("hash");
        alice = userRepository.save(alice);

        var bob = new User();
        bob.setUsername("bob-it");
        bob.setEmail("bob-it@test.local");
        bob.setPassword("hash");
        bob = userRepository.save(bob);

        var accAlice = new Account();
        accAlice.setUser(alice);
        accAlice.setAccountName("Alice Main");
        accAlice.setCurrency("EUR");
        accAlice.setBalance(new BigDecimal("200.00"));
        accAlice = accountRepository.save(accAlice);

        var accBob = new Account();
        accBob.setUser(bob);
        accBob.setAccountName("Bob Main");
        accBob.setCurrency("EUR");
        accBob.setBalance(new BigDecimal("10.00"));
        accBob = accountRepository.save(accBob);

        var buddyLink = new UserConnection();
        buddyLink.setId(new UserConnectionId(alice.getId(), bob.getId()));
        buddyLink.setOwner(alice);
        buddyLink.setRelated(bob);
        userConnectionRepository.save(buddyLink);

        var dto = new TransactionCreateDTO(
                accAlice.getId(),
                bob.getEmail(),
                new BigDecimal("100.00"),
                "it-no-seed-1",
                "test"
        );
        var result = transactionService.create(dto);

        assertThat(result.fee()).isEqualByComparingTo("0.50");

        var refreshedAlice = accountRepository.findById(accAlice.getId()).orElseThrow();
        var refreshedBob = accountRepository.findById(accBob.getId()).orElseThrow();
        var systemFeesAccount = accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(
                "system@paymybuddy.local",
                "PayMyBuddy Fees"
        ).orElseThrow();

        var initialTotal = new BigDecimal("210.00");
        var finalTotal = refreshedAlice.getBalance()
                .add(refreshedBob.getBalance())
                .add(systemFeesAccount.getBalance());

        assertThat(refreshedAlice.getBalance()).isEqualByComparingTo("99.50");
        assertThat(refreshedBob.getBalance()).isEqualByComparingTo("110.00");
        assertThat(systemFeesAccount.getBalance()).isEqualByComparingTo("0.50");
        assertThat(finalTotal).isEqualByComparingTo(initialTotal);
    }
}
