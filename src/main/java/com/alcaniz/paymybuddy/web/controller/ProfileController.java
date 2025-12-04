// Java
package com.alcaniz.paymybuddy.web.controller;

import com.alcaniz.paymybuddy.service.crud.UserService;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        String email = principal.getName();
        UserDTO user = userService.getByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Utilisateur non trouvé pour " + email));
        model.addAttribute("user", user);
        return "profile";
    }
}