// Java
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.AccountService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.dto.account.AccountDTO;
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
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Integer userId = currentUserId(principal);
        List<AccountDTO> accounts = accountService.getAllForUser(userId);
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountForm", new AccountCreateDTO(userId, "", "EUR"));
        return "accounts";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("accountForm") AccountCreateDTO form,
                         BindingResult bindingResult,
                         Model model,
                         Principal principal) {
        Integer userId = currentUserId(principal);
        if (bindingResult.hasErrors()) {
            model.addAttribute("accounts", accountService.getAllForUser(userId));
            return "accounts";
        }
        accountService.create(form);
        return "redirect:/accounts";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id) {
        accountService.deleteById(id);
        return "redirect:/accounts";
    }

    private Integer currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).map(UserDTO::id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
    }
}
