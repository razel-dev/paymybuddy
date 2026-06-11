package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.AccountService;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionCreateDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/transfer")
public class TransactionController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String form(Model model, Principal principal) {
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("transferForm", newTransferForm());
        model.addAttribute("history", accounts.isEmpty() ? List.of()
                : transactionService.getHistoryForAccount(accounts.getFirst().id()));
        return "transfer";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("transferForm") TransactionCreateDTO form,
                         BindingResult bindingResult,
                         Model model,
                         Principal principal) {
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accounts);
            model.addAttribute("history", historyForOwnedAccount(accounts, form.senderAccountId()));
            return "transfer";
        }

        if (!ownsAccount(accounts, form.senderAccountId())) {
            bindingResult.rejectValue(
                    "senderAccountId",
                    "transfer.senderAccount.forbidden",
                    "Le compte emetteur n'appartient pas a l'utilisateur connecte"
            );
            model.addAttribute("accounts", accounts);
            model.addAttribute("history", historyForOwnedAccount(accounts, form.senderAccountId()));
            return "transfer";
        }

        try {
            transactionService.create(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("transfer.error", ex.getMessage());
            model.addAttribute("accounts", accounts);
            model.addAttribute("history", historyForOwnedAccount(accounts, form.senderAccountId()));
            return "transfer";
        }

        return "redirect:/transfer";
    }

    private boolean ownsAccount(List<AccountDTO> accounts, Integer senderAccountId) {
        return senderAccountId != null
                && accounts.stream().map(AccountDTO::id).anyMatch(senderAccountId::equals);
    }

    private List<TransactionDTO> historyForOwnedAccount(List<AccountDTO> accounts, Integer senderAccountId) {
        if (accounts.isEmpty()) {
            return List.of();
        }
        Integer accountIdForHistory = ownsAccount(accounts, senderAccountId)
                ? senderAccountId
                : accounts.getFirst().id();
        return transactionService.getHistoryForAccount(accountIdForHistory);
    }

    private Integer currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).map(UserDTO::id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouve"));
    }

    private TransactionCreateDTO newTransferForm() {
        return new TransactionCreateDTO(null, null, new BigDecimal("1.00"), UUID.randomUUID().toString(), "");
    }
}
