package com.alcaniz.paymybuddy.it;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.BankTransferRepository;
import com.alcaniz.paymybuddy.repository.UserRepository;
import com.alcaniz.paymybuddy.service.crud.BankTransferService;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doAnswer;

@ActiveProfiles("test")
@SpringBootTest
@Import(MySqlTestcontainersConfig.class)
class ConcurrentBankTransferIT {

    @Autowired
    private UserRepository userRepository;

    @SpyBean
    private AccountRepository accountRepository;

    @Autowired
    private BankTransferRepository bankTransferRepository;

    @Autowired
    private BankTransferService bankTransferService;

    @Test
    void concurrent_withdrawals_should_allow_only_one_success_when_funds_are_insufficient_for_both() throws Exception {
        User owner = new User();
        owner.setUsername("concurrent-owner");
        owner.setEmail("concurrent-owner@test.local");
        owner.setPassword("hash");
        owner = userRepository.save(owner);

        Account account = new Account();
        account.setUser(owner);
        account.setAccountName("Concurrent Main");
        account.setCurrency("EUR");
        account.setBalance(new BigDecimal("100.00"));
        account = accountRepository.save(account);

        Integer accountId = account.getId();
        CountDownLatch bothReadsReached = new CountDownLatch(2);
        AtomicInteger gatedReads = new AtomicInteger();

        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Optional<Account> result = (Optional<Account>) invocation.callRealMethod();
            Integer requestedId = invocation.getArgument(0);
            if (Objects.equals(requestedId, accountId) && gatedReads.incrementAndGet() <= 2) {
                bothReadsReached.countDown();
                assertTrue(bothReadsReached.await(5, TimeUnit.SECONDS));
            }
            return result;
        }).when(accountRepository).findById(accountId);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Throwable> first = executor.submit(() -> runWithdrawal(accountId, "80.00"));
            Future<Throwable> second = executor.submit(() -> runWithdrawal(accountId, "30.00"));

            Throwable firstError = first.get(10, TimeUnit.SECONDS);
            Throwable secondError = second.get(10, TimeUnit.SECONDS);

            List<Throwable> errors = List.of(firstError, secondError).stream()
                    .filter(Objects::nonNull)
                    .toList();

            Account refreshed = accountRepository.findById(accountId).orElseThrow();
            List<?> history = bankTransferRepository.findAllByAccount_IdOrderByCreatedAtDesc(accountId);

            assertThat(errors).hasSize(1);
            assertThat(errors.getFirst()).isInstanceOf(BusinessException.class);
            assertThat(history).hasSize(1);
            assertThat(refreshed.getBalance())
                    .isIn(new BigDecimal("20.00"), new BigDecimal("70.00"));
        } finally {
            executor.shutdownNow();
        }
    }

    private Throwable runWithdrawal(Integer accountId, String amount) {
        try {
            bankTransferService.create(new BankTransferCreateDTO(
                    accountId,
                    new BigDecimal(amount),
                    BankTransferCreateDTO.BankTransferType.WITHDRAWAL
            ));
            return null;
        } catch (Throwable throwable) {
            return throwable;
        }
    }
}
