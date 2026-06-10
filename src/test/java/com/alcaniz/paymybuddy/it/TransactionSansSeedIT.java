package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
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
@Transactional // chaque test est isolé (rollback en fin de test)
class TransactionSansSeedIT {

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private AccountRepository accountRepository;
  @Autowired
  private TransactionService transactionService;    // Service métier qui applique les frais et met à jour les soldes

  @Test
  void transfert_applique_frais_et_met_a_jour_les_soldes() {
    // 1) Arrange: créer les utilisateurs et leurs comptes avec soldes initiaux
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

    // 2) Act: exécuter un transfert via le service (ex: 100.00 avec frais 0,5% = 0,50)
    var dto = new TransactionCreateDTO(accAlice.getId(), bob.getEmail(), new BigDecimal("100.00"), "test");
    var result = transactionService.create(dto);

    // 3) Assert: vérifier frais calculés et soldes mis à jour côté base
    assertThat(result.fee()).isEqualByComparingTo("0.50");

    var refreshedAlice = accountRepository.findById(accAlice.getId()).orElseThrow();
    var refreshedBob   = accountRepository.findById(accBob.getId()).orElseThrow();

    // Alice débitée du montant + frais => 200 - (100 + 0,50) = 99,50
    assertThat(refreshedAlice.getBalance()).isEqualByComparingTo("99.50");
    // Bob crédité du montant => 10 + 100 = 110,00
    assertThat(refreshedBob.getBalance()).isEqualByComparingTo("110.00");
  }
}
