package com.clareza;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base dos testes que precisam do banco real. O container e iniciado uma unica vez por JVM e
 * compartilhado por todas as subclasses — com @Testcontainers o ciclo seria por classe, e cada
 * nova classe custaria mais um Postgres subindo do zero.
 */
@SpringBootTest
public abstract class TesteDeIntegracao {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("clareza")
                    .withUsername("clareza")
                    .withPassword("clareza");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configurarDatasource(DynamicPropertyRegistry registro) {
        registro.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registro.add("spring.datasource.username", POSTGRES::getUsername);
        registro.add("spring.datasource.password", POSTGRES::getPassword);
    }
}