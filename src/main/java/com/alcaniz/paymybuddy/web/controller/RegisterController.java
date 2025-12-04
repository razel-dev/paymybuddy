// Java
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegisterController {

    private final UserService userService;

    @GetMapping("/register")
    public String form(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new UserCreateDTO("", "", ""));
        }
        return "register";
    }

    @PostMapping("/register")
    public String submit(@Valid @ModelAttribute("form") UserCreateDTO form,
                         BindingResult binding) {
        if (binding.hasErrors()) {
            return "register";
        }
        // Crée l'utilisateur; les validations métier et l'encodage du mot de passe
        // sont gérés dans UserService.
        userService.create(form);
        // Redirige vers /login avec un indicateur de succès
        return "redirect:/login?registered";
    }
}