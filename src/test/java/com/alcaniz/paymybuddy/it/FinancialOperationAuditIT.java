package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.FinancialOperationSourceType;
import com.alcaniz.paymybuddy.model.FinancialOperationType;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.model.UserConnection;
import com.alcaniz.paymybuddy.model.UserConnectionId;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.BankTransferRepository;
import com.alcaniz.paymybuddy.repository.FinancialOperationAuditRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.BankTransferService;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:audit-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class FinancialOperationAuditIT {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserConnectionRepository userConnectionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BankTransferRepository bankTransferRepository;

    @Autowired
    private FinancialOperationAuditRepository financialOperationAuditRepository;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private BankTransferService bankTransferService;

    @BeforeEach
    void cleanDatabase() {
        financialOperationAuditRepository.deleteAll();
        bankTransferRepository.deleteAll();
        transactionRepository.deleteAll();
        userConnectionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void transfer_persisteUneTraceAuditImmuable() {
        var alice = user("alice-audit", "alice-audit@test.local");
        var bob = user("bob-audit", "bob-audit@test.local");
        var system = user("system-audit", "system@paymybuddy.local");

        var aliceAccount = account(alice, "Alice Main", "EUR", "200.00");
        var bobAccount = account(bob, "Bob Main", "EUR", "10.00");
        account(system, "PayMyBuddy Fees", "EUR", "0.00");
        userConnection(alice, bob);

        var dto = new TransactionCreateDTO(
                aliceAccount.getId(),
                bob.getEmail(),
                new BigDecimal("100.00"),
                "audit-transfer-1",
                "audit p2p"
        );

        transactionService.create(dto);

        var audits = financialOperationAuditRepository.findAll();
        assertThat(audits).hasSize(1);

        var audit = audits.getFirst();
        assertThat(audit.getOperationType()).isEqualTo(FinancialOperationType.P2P_TRANSFER);
        assertThat(audit.getSourceType()).isEqualTo(FinancialOperationSourceType.TRANSACTION);
        assertThat(audit.getActorUserId()).isEqualTo(alice.getId());
        assertThat(audit.getAccountId()).isEqualTo(aliceAccount.getId());
        assertThat(audit.getCounterpartyAccountId()).isEqualTo(bobAccount.getId());
        assertThat(audit.getAmount()).isEqualByComparingTo("100.00");
        assertThat(audit.getFee()).isEqualByComparingTo("0.50");
        assertThat(audit.getCurrency()).isEqualTo("EUR");
        assertThat(audit.getDescription()).isEqualTo("audit p2p");
        assertThat(audit.getOccurredAt()).isNotNull();
    }

    @Test
    void depot_persisteUneTraceAuditImmuable() {
        var alice = user("alice-deposit", "alice-deposit@test.local");
        var aliceAccount = account(alice, "Alice Main", "EUR", "25.00");

        var dto = new BankTransferCreateDTO(
                aliceAccount.getId(),
                new BigDecimal("50.00"),
                BankTransferCreateDTO.BankTransferType.DEPOSIT
        );

        bankTransferService.create(dto);

        var audits = financialOperationAuditRepository.findAll();
        assertThat(audits).hasSize(1);

        var audit = audits.getFirst();
        assertThat(audit.getOperationType()).isEqualTo(FinancialOperationType.BANK_DEPOSIT);
        assertThat(audit.getSourceType()).isEqualTo(FinancialOperationSourceType.BANK_TRANSFER);
        assertThat(audit.getActorUserId()).isEqualTo(alice.getId());
        assertThat(audit.getAccountId()).isEqualTo(aliceAccount.getId());
        assertThat(audit.getCounterpartyAccountId()).isNull();
        assertThat(audit.getAmount()).isEqualByComparingTo("50.00");
        assertThat(audit.getFee()).isEqualByComparingTo("0.00");
        assertThat(audit.getCurrency()).isEqualTo("EUR");
        assertThat(audit.getDescription()).isEqualTo("DEPOSIT");
        assertThat(audit.getOccurredAt()).isNotNull();
    }

    private User user(String username, String email) {
        var user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("hash");
        return userRepository.save(user);
    }

    private Account account(User user, String name, String currency, String balance) {
        var account = new Account();
        account.setUser(user);
        account.setAccountName(name);
        account.setCurrency(currency);
        account.setBalance(new BigDecimal(balance));
        return accountRepository.save(account);
    }

    private void userConnection(User owner, User related) {
        var connection = new UserConnection();
        connection.setId(new UserConnectionId(owner.getId(), related.getId()));
        connection.setOwner(owner);
        connection.setRelated(related);
        userConnectionRepository.save(connection);
    }
}
