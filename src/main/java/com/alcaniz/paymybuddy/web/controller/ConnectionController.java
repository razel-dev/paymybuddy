package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.ConnectionService;
import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.connection.ConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/connections")
public class ConnectionController {

    private final ConnectionService connectionService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Principal principal) {
        Integer userId = currentUserId(principal);
        List<ConnectionDTO> connections = connectionService.getAllForOwner(userId);
        model.addAttribute("connections", connections);
        model.addAttribute("connectionForm", new UserConnectionDTO(userId, null));
        return "connections";
    }

    @PostMapping
    public String add(@RequestParam(name = "email", required = false) String email,
                      @Valid @ModelAttribute(value = "connectionForm", binding = false) UserConnectionDTO ignoredForm,
                      BindingResult bindingResult,
                      Model model,
                      Principal principal) {
        Integer userId = currentUserId(principal);
        if (email == null || email.trim().isEmpty()) {
            model.addAttribute("connections", connectionService.getAllForOwner(userId));
            model.addAttribute("error", "Veuillez saisir une adresse e‑mail.");
            return "connections";
        }
        String normalizedEmail = email.trim().toLowerCase();
        Optional<UserDTO> relatedOpt = userService.getByEmail(normalizedEmail);
        if (relatedOpt.isEmpty()) {
            model.addAttribute("connections", connectionService.getAllForOwner(userId));
            model.addAttribute("error", "Utilisateur introuvable pour: " + normalizedEmail);
            return "connections";
        }
        Integer relatedUserId = relatedOpt.get().id();
        if (relatedUserId.equals(userId)) {
            model.addAttribute("connections", connectionService.getAllForOwner(userId));
            model.addAttribute("error", "Vous ne pouvez pas vous ajouter vous‑même.");
            return "connections";
        }
        if (connectionService.exists(userId, relatedUserId)) {
            model.addAttribute("connections", connectionService.getAllForOwner(userId));
            model.addAttribute("info", "Cette relation existe déjà.");
            return "connections";
        }
        connectionService.create(new UserConnectionDTO(userId, relatedUserId));
        return "redirect:/connections";
    }

    private Integer currentUserId(Principal principal) {
        String email = principal.getName();
        return userService.getByEmail(email).map(UserDTO::id)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé"));
    }
}
