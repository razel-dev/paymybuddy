package com.alcaniz.paymybuddy.infra;
import com.alcaniz.paymybuddy.service.crud.*;
import com.alcaniz.paymybuddy.web.dto.account.AccountCreateDTO;
import com.alcaniz.paymybuddy.web.dto.connection.UserConnectionDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserCreateDTO;
import com.alcaniz.paymybuddy.web.dto.user.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;



@Configuration
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DemoDataInitializer {

    private final UserService userService;
    private final AccountService accountService;
    private final ConnectionService connectionService;

    private UserDTO ensureUser(String username, String email, String rawPassword) {
        return userService.getByEmail(email)
                .orElseGet(() -> userService.create(new UserCreateDTO(username, email, rawPassword)));
    }

    @Bean
        CommandLineRunner seedDemoData() {
        return args -> {
            log.info("Initialisation des données de démonstration (profil 'demo')");

            UserDTO demo = ensureUser("demo", "demo@local.test", "demo1234");
            UserDTO buddy = ensureUser("buddy", "buddy@local.test", "demo12345");

            var demoAccounts = accountService.getAllForUser(demo.id());
            if (demoAccounts.isEmpty()) {
                accountService.create(new AccountCreateDTO(demo.id(), "Compte principal", "EUR"));
            }

            var buddyAccounts = accountService.getAllForUser(buddy.id());
            if (buddyAccounts.isEmpty()) {
                accountService.create(new AccountCreateDTO(buddy.id(), "Compte principal", "EUR"));
            }

            try {
                connectionService.create(new UserConnectionDTO(demo.id(), buddy.id()));

            } catch (Exception e) {
                log.debug("Connexion demo -> buddy déjà présente ou ignorée: {}", e.getMessage());
            }

            log.info("Comptes prêts pour la démo sans dépôt initial: demo@local.test / buddy@local.test (mdp: demo1234)");
        };
    }


}