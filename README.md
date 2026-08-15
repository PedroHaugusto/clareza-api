# Clareza — API

API do sistema de organização financeira pessoal Clareza.

## Stack

- Java 8
- Spring Boot 2.7.18
- PostgreSQL 16 + Flyway
- Arquitetura hexagonal (Clean Architecture)

## Requisitos

- JDK 8
- Maven 3.8+ (ou use o wrapper `./mvnw`)
- Docker (para o banco local)

## Rodando localmente

Copie o arquivo de exemplo de variáveis de ambiente:

```bash
cp .env.example .env
```

### Opção 1 — banco no Docker, aplicação na IDE

Fluxo do dia a dia: sobe só o PostgreSQL e roda a aplicação direto, com hot reload.

```bash
docker compose up -d db
./mvnw spring-boot:run
```

### Opção 2 — stack completo no Docker

```bash
docker compose up --build
```

A API fica em `http://localhost:8080` e o banco em `localhost:5432`
(banco/usuário/senha: `clareza`).

Para encerrar:

```bash
docker compose down      # preserva os dados
docker compose down -v   # descarta o volume também
```

Para conferir se subiu:

```bash
curl http://localhost:8080/actuator/health   # {"status":"UP"}
```

O CORS libera `http://localhost:4200` por padrão, e só nas rotas `/api/**`. Para liberar outras
origens, use `CORS_ALLOWED_ORIGINS` com a lista separada por vírgula.

## Autenticação

`/actuator/health` e as rotas `/api/auth/**` são públicas. Todo o resto exige um JWT emitido
pela própria API, enviado em `Authorization: Bearer <token>`.

```bash
# cria a conta e ja devolve o token
curl -X POST http://localhost:8080/api/auth/registrar \
  -H "Content-Type: application/json" \
  -d '{"nome":"Ana","email":"ana@clareza.dev","senha":"senha-secreta"}'

# login posterior
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ana@clareza.dev","senha":"senha-secreta"}'
```

O token expira em `JWT_EXPIRATION_MINUTES` (60 por padrão) e é assinado com `JWT_SECRET`, que
precisa de no mínimo 32 caracteres. Trocar o segredo invalida todos os tokens já emitidos.

> A aplicação ainda registra `Using generated security password: ...` no boot. Essa senha não
> serve para nada: não há basic auth nem formulário de login na cadeia de filtros, só o JWT.

## Testes

```bash
./mvnw test
```

Os testes de integração sobem um PostgreSQL 16 real via Testcontainers — o Docker precisa estar
rodando. O `pom.xml` fixa `api.version=1.43` no surefire: o Testcontainers 1.19.x negocia a API
1.32 por padrão, versão que o Docker Engine 29 já removeu.

## Status

Bloco 1 concluído — ambiente local em Docker, configuração da aplicação e primeiro teste de
integração funcionando. Próximo: Bloco 2 (tratamento de erro centralizado, CORS e formato
padrão de resposta).