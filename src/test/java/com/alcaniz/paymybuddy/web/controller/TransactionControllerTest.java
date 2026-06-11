package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.AccountService;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AccountService accountService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController controller;

    @Test
    void submit_shouldRejectTransferWhenSenderAccountDoesNotBelongToAuthenticatedUser() {
        var authenticatedUser = new UserDTO(1, "alice", "alice@example.com", Instant.now());
        var ownedAccount = new AccountDTO(10, 1, "Alice main account", "EUR", new BigDecimal("250.00"), Instant.now());
        var forgedForm = new TransactionCreateDTO(
                999,
                "bob@example.com",
                new BigDecimal("25.00"),
                "idem-forged-1",
                "forged transfer"
        );
        BindingResult bindingResult = new BeanPropertyBindingResult(forgedForm, "transferForm");
        var model = new ConcurrentModel();
        Principal principal = () -> "alice@example.com";

        when(userService.getByEmail("alice@example.com")).thenReturn(Optional.of(authenticatedUser));
        when(accountService.getAllForUser(1)).thenReturn(List.of(ownedAccount));
        when(transactionService.getHistoryForAccount(10)).thenReturn(List.of());

        var view = controller.submit(forgedForm, bindingResult, model, principal);

        assertAll(
                () -> assertEquals("transfer", view),
                () -> assertTrue(bindingResult.hasFieldErrors("senderAccountId")),
                () -> verify(transactionService, never()).create(any()),
                () -> verify(transactionService).getHistoryForAccount(10)
        );
    }
}
