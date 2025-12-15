
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.AccountService;
import com.alcaniz.paymybuddy.service.crud.TransactionService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
import com.alcaniz.paymybuddy.web.dto.transaction.TransactionDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class DashboardController {

    private final UserService userService;
    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String index(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);
        model.addAttribute("accounts", accounts);

        // Historique du premier compte si disponible (prototype)
        if (!accounts.isEmpty()) {
            Integer accountId = accounts.getFirst().id();
            List<TransactionDTO> recent = transactionService.getHistoryForAccount(accountId);
            model.addAttribute("recentTransactions", recent);
            model.addAttribute("selectedAccountId", accountId);
        }
        return "dashboard";
    }

    private Integer currentUserId(Principal principal) {
        String email = principal.getName();
        Optional<UserDTO> u = userService.getByEmail(email);
        return u.map(UserDTO::id).orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé pour " + email));
    }
}
