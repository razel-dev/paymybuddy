// Java
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import com.alcaniz.paymybuddy.web.exception.BadRequestException;
import com.alcaniz.paymybuddy.web.exception.BusinessException;
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
                         BindingResult binding,
                         Model model) {
        if (binding.hasErrors()) {
            return "register";
        }
        try {
            userService.create(form);
        } catch (BadRequestException e) {
            // Erreurs de validation côté service (ex. champs vides)
            binding.reject("bad_request", e.getMessage());
            return "register";
        } catch (BusinessException e) {
            // Erreurs métier (ex. email déjà utilisé)
            binding.reject("business_error", e.getMessage());
            return "register";
        }
        return "redirect:/login?registered";
    }
}