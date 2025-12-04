// Java
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.AccountService;
import com.alcaniz.paymybuddy.service.crud.BankTransferService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import com.alcaniz.paymybuddy.web.dto.banktransfer.BankTransferCreateDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bank")
public class BankTransferController {

    private final UserService userService;
    private final AccountService accountService;
    private final BankTransferService bankTransferService;

    @GetMapping
    public String form(Model model, Principal principal) {
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("bankForm",
                new BankTransferCreateDTO(accounts.isEmpty() ? null : accounts.getFirst().id(),
                        new java.math.BigDecimal("1.00"),
                        BankTransferCreateDTO.BankTransferType.DEPOSIT));
        model.addAttribute("history", accounts.isEmpty() ? java.util.List.of()
                : bankTransferService.getHistoryForAccount(accounts.getFirst().id()));
        return "bank";
    }

    @PostMapping
    public String submit(@Valid @ModelAttribute("bankForm") BankTransferCreateDTO form,
                         BindingResult bindingResult,
                         Model model,
                         Principal principal) {
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accounts);
            model.addAttribute("history", accounts.isEmpty() ? java.util.List.of()
                    : bankTransferService.getHistoryForAccount(accounts.getFirst().id()));
            return "bank";
        }
        bankTransferService.create(form);
        return "redirect:/bank";
    }

    private Integer currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).map(UserDTO::id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
    }
}
