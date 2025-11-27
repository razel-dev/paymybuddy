package com.alcaniz.paymybuddy.infra;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

@TestConfiguration(proxyBeanMethods = false)
public class MySqlTestcontainersConfig {

    @Bean
    @ServiceConnection //  injecte automatiquement les propriétés datasource
    MySQLContainer<?> mysqlContainer() {
        return new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("paymybuddy")
                .withUsername("test")
                .withPassword("test");
    }
}