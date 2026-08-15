package com.clareza;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Sobe o contexto completo contra um PostgreSQL real, conferindo de uma vez o datasource,
 * o pool do Hikari, o Flyway e o ddl-auto=validate.
 */
@SpringBootTest
@Testcontainers
class ClarezaApplicationTests {

    // Mesma versao do docker-compose: o Flyway 9.x recusa a conexao com PostgreSQL 17 ou mais novo.
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("clareza")
                    .withUsername("clareza")
                    .withPassword("clareza");

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void deveCarregarOContextoDaAplicacao() {
        // O proprio carregamento do contexto e a asercao: qualquer erro de configuracao falha aqui.
    }
}