package com.alcaniz.paymybuddy;

import com.alcaniz.paymybuddy.infra.MySqlTestcontainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(MySqlTestcontainersConfig.class)
class PaymybuddyApplicationTests {

    @Test
    void contextLoads() {
    }

}
