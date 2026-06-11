package com.alcaniz.paymybuddy.service.impl;

import com.alcaniz.paymybuddy.model.Account;
import com.alcaniz.paymybuddy.model.Transaction;
import com.alcaniz.paymybuddy.model.User;
import com.alcaniz.paymybuddy.repository.AccountRepository;
import com.alcaniz.paymybuddy.repository.TransactionRepository;
import com.alcaniz.paymybuddy.repository.UserConnectionRepository;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final String SYSTEM_EMAIL = "system@paymybuddy.local";
    private static final String SYSTEM_FEES_ACCOUNT_NAME = "PayMyBuddy Fees";

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    UserConnectionRepository userConnectionRepository;

    @Mock
    TransactionMapper transactionMapper;

    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    com.alcaniz.paymybuddy.service.crud.impl.TransactionServiceImpl service;

    @BeforeEach
    void setDefaults() {
        ReflectionTestUtils.setField(service, "maxTransferAmount", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(service, "dailyMaxTransferCount", 10);
        ReflectionTestUtils.setField(service, "dailyMaxTransferAmount", new BigDecimal("3000.00"));
    }

    private static Account acc(int accountId, int userId, String email, String balance, String currency) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);

        Account account = new Account();
        account.setUser(user);
        account.setAccountName("acc-" + accountId);
        account.setCurrency(currency);
        account.setBalance(new BigDecimal(balance));
        account.setId(accountId);
        return account;
    }

    private static Account systemAccount(int accountId, String balance) {
        User user = new User();
        user.setId(999);
        user.setEmail(SYSTEM_EMAIL);

        Account account = new Account();
        account.setUser(user);
        account.setAccountName(SYSTEM_FEES_ACCOUNT_NAME);
        account.setCurrency("EUR");
        account.setBalance(new BigDecimal(balance));
        account.setId(accountId);
        return account;
    }

    private static TransactionCreateDTO createDto(int senderAccountId, String receiverEmail, String amount, String key, String description) {
        return new TransactionCreateDTO(senderAccountId, receiverEmail, new BigDecimal(amount), key, description);
    }

    private static Transaction baseTxForMapper(BigDecimal amount, String idempotencyKey) {
        Transaction tx = new Transaction();
        tx.setSenderAccount(new Account());
        tx.setReceiverAccount(new Account());
        tx.setAmount(amount);
        tx.setFee(BigDecimal.ZERO);
        tx.setIdempotencyKey(idempotencyKey);
        return tx;
    }

    private void stubNoExistingIdempotency(String key) {
        when(transactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
    }

    private void stubHappyDailyLimits() {
        when(transactionRepository.countBySenderAccount_User_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(anyInt(), any(), any()))
                .thenReturn(0L);
        when(transactionRepository.sumAmountBySenderUserIdAndCreatedAtBetween(anyInt(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
    }

    @Test
    void create_rejoueTransactionExistanteQuandIdempotencyKeyExiste() {
        var dto = createDto(1, "bob@example.com", "100.00", "idem-1", "duplicate");
        var existing = new Transaction();
        var dtoOut = mock(TransactionDTO.class);

        when(transactionRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));
        when(transactionMapper.toDto(existing)).thenReturn(dtoOut);

        var result = service.create(dto);

        assertSame(dtoOut, result);
        verify(accountRepository, never()).findById(anyInt());
        verify(transactionRepository, never()).save(any(Transaction.class));
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void create_rejetteDestinataireNonBuddy() {
        var dto = createDto(1, "mallory@example.com", "25.00", "idem-2", "x");
        var sender = acc(1, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "mallory@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-2");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("mallory@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_rejetteTransfertInterDevises() {
        var dto = createDto(1, "bob@example.com", "25.00", "idem-3", "fx");
        var sender = acc(1, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "USD");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-3");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_rejetteMontantAuDessusDuPlafondConfigure() {
        ReflectionTestUtils.setField(service, "maxTransferAmount", new BigDecimal("500.00"));

        var dto = createDto(1, "bob@example.com", "600.00", "idem-4", "cap");
        var sender = acc(1, 101, "alice@example.com", "1000.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-4");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount(), dto.idempotencyKey()));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_rejetteNombreMaxDeTransfertsJournalierAtteint() {
        ReflectionTestUtils.setField(service, "dailyMaxTransferCount", 3);

        var dto = createDto(1, "bob@example.com", "25.00", "idem-5", "aml-count");
        var sender = acc(1, 101, "alice@example.com", "1000.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-5");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount(), dto.idempotencyKey()));
        when(transactionRepository.countBySenderAccount_User_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(anyInt(), any(), any()))
                .thenReturn(3L);

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_rejetteMontantCumuleJournalierAuDessusDuPlafond() {
        ReflectionTestUtils.setField(service, "dailyMaxTransferAmount", new BigDecimal("700.00"));

        var dto = createDto(1, "bob@example.com", "250.00", "idem-6", "aml-amount");
        var sender = acc(1, 101, "alice@example.com", "1000.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-6");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount(), dto.idempotencyKey()));
        when(transactionRepository.countBySenderAccount_User_IdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(anyInt(), any(), any()))
                .thenReturn(2L);
        when(transactionRepository.sumAmountBySenderUserIdAndCreatedAtBetween(anyInt(), any(), any()))
                .thenReturn(new BigDecimal("500.00"));

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void create_verrouilleLesTroisComptesDansOrdreCroissant() {
        var dto = createDto(5, "bob@example.com", "10.00", "idem-7", "ordered lock");
        var sender = acc(5, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-7");
        when(accountRepository.findById(5)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(5, 2, 9000)))
                .thenReturn(List.of(receiver, sender, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount(), dto.idempotencyKey()));
        stubHappyDailyLimits();

        var saved = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);
        when(transactionMapper.toDto(saved)).thenReturn(mock(TransactionDTO.class));

        service.create(dto);

        verify(accountRepository).findAllByIdInOrderByIdAsc(List.of(5, 2, 9000));
    }

    @Test
    void create_soldeInsuffisant_lanceErreurEtPasDeSave() {
        var dto = createDto(1, "bob@example.com", "50.00", "idem-8", "x");
        var sender = acc(1, 101, "alice@example.com", "50.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "0.00", "EUR");
        var feesAccount = systemAccount(9000, "0.00");

        stubNoExistingIdempotency("idem-8");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        when(transactionMapper.toEntity(dto)).thenReturn(baseTxForMapper(dto.amount(), dto.idempotencyKey()));
        stubHappyDailyLimits();

        assertThrows(IllegalArgumentException.class, () -> service.create(dto));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(accountRepository, never()).save(any(Account.class));
        assertEquals(new BigDecimal("50.00"), sender.getBalance());
        assertEquals(new BigDecimal("0.00"), receiver.getBalance());
        assertEquals(new BigDecimal("0.00"), feesAccount.getBalance());
    }

    @Test
    void create_happyPath_crediteLesFraisSurLeCompteSystemeEtConserveLaSommeDesSoldes() {
        var dto = createDto(1, "bob@example.com", "100.00", "idem-9", "desc");
        var sender = acc(1, 101, "alice@example.com", "200.00", "EUR");
        var receiver = acc(2, 202, "bob@example.com", "10.00", "EUR");
        var feesAccount = systemAccount(9000, "5.00");
        BigDecimal initialTotal = sender.getBalance().add(receiver.getBalance()).add(feesAccount.getBalance());

        stubNoExistingIdempotency("idem-9");
        when(accountRepository.findById(1)).thenReturn(Optional.of(sender));
        when(accountRepository.findFirstByUser_EmailOrderByIdAsc("bob@example.com")).thenReturn(Optional.of(receiver));
        when(accountRepository.findFirstByUser_EmailAndAccountNameOrderByIdAsc(SYSTEM_EMAIL, SYSTEM_FEES_ACCOUNT_NAME))
                .thenReturn(Optional.of(feesAccount));
        when(accountRepository.findAllByIdInOrderByIdAsc(List.of(1, 2, 9000)))
                .thenReturn(List.of(sender, receiver, feesAccount));
        when(userConnectionRepository.existsByOwner_IdAndRelated_Id(101, 202)).thenReturn(true);
        stubHappyDailyLimits();

        Transaction base = baseTxForMapper(dto.amount(), dto.idempotencyKey());
        when(transactionMapper.toEntity(dto)).thenReturn(base);

        var saved = new Transaction();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        var dtoOut = mock(TransactionDTO.class);
        when(transactionMapper.toDto(saved)).thenReturn(dtoOut);

        var res = service.create(dto);
        BigDecimal finalTotal = sender.getBalance().add(receiver.getBalance()).add(feesAccount.getBalance());

        assertSame(dtoOut, res);
        org.assertj.core.api.Assertions.assertThat(sender.getBalance()).isEqualByComparingTo("99.50");
        org.assertj.core.api.Assertions.assertThat(receiver.getBalance()).isEqualByComparingTo("110.00");
        org.assertj.core.api.Assertions.assertThat(feesAccount.getBalance()).isEqualByComparingTo("5.50");
        org.assertj.core.api.Assertions.assertThat(finalTotal).isEqualByComparingTo(initialTotal);

        verify(accountRepository, times(3)).save(any(Account.class));
        verify(transactionRepository).save(any(Transaction.class));
        verify(transactionMapper).toDto(saved);
        verify(applicationEventPublisher).publishEvent(any(Object.class));
    }

    @Test
    void getById_ok_mappeVersDto() {
        var entity = mock(Transaction.class);
        when(transactionRepository.findById(7)).thenReturn(Optional.of(entity));
        var dto = mock(TransactionDTO.class);
        when(transactionMapper.toDto(entity)).thenReturn(dto);

        var res = service.getById(7);

        assertTrue(res.isPresent());
        assertSame(dto, res.orElseThrow());
        verify(transactionRepository).findById(7);
        verify(transactionMapper).toDto(entity);
    }
}
