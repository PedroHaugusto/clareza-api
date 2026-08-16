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

### Fuso horário

`APP_TIMEZONE` (padrão `America/Sao_Paulo`) define o que a API considera **hoje** — o que decide
se uma conta está atrasada, qual é o mês do calendário, a janela de vencimentos e o saldo
disponível. As datas de negócio saem de um `Clock` configurado, não do relógio do servidor:
rodando em UTC, das 21h à meia-noite o servidor já estaria no dia seguinte e marcaria como
atrasado o que ainda vence hoje.

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

### Login com Google

O frontend obtém o `id_token` pelo Google Identity Services e o envia para a API, que confere a
assinatura contra as chaves públicas do Google e devolve o JWT da própria aplicação:

```bash
curl -X POST http://localhost:8080/api/auth/google \
  -H "Content-Type: application/json" \
  -d '{"idToken":"<id_token do Google>"}'
```

Primeiro acesso cria a conta sem senha. Se o e-mail já tiver cadastro com senha, a conta recebe
o vínculo com o Google e passa a aceitar os dois caminhos de login.

Depende de `GOOGLE_CLIENT_ID`. Sem essa variável o endpoint responde 401 avisando que o login
social não está configurado — os demais endpoints continuam funcionando normalmente.

> A aplicação ainda registra `Using generated security password: ...` no boot. Essa senha não
> serve para nada: não há basic auth nem formulário de login na cadeia de filtros, só o JWT.

## Testes

```bash
./mvnw test
```

Os testes de integração sobem um PostgreSQL 16 real via Testcontainers — o Docker precisa estar
rodando. O `pom.xml` fixa `api.version=1.43` no surefire: o Testcontainers 1.19.x negocia a API
1.32 por padrão, versão que o Docker Engine 29 já removeu.

## Deploy

A cada push na `main`, o GitHub Actions roda build e testes; **só se tudo passar** ele chama o
deploy hook do Render. Pull request roda os testes, mas não publica.

### 1. Banco no Neon

Crie o projeto **como PostgreSQL 16** — o Flyway 9.x recusa a conexão com versões mais novas.
O Neon entrega a string no formato `postgresql://usuario:senha@host/banco?sslmode=require`, que
**não** é aceita direto: a aplicação espera JDBC, com usuário e senha separados.

| Variável | Valor a partir da string do Neon |
|---|---|
| `DATABASE_URL` | `jdbc:postgresql://host/banco?sslmode=require` |
| `DATABASE_USER` | o usuário |
| `DATABASE_PASSWORD` | a senha |

O Flyway cria todo o schema no primeiro boot; não é preciso rodar nada à mão.

### 2. API no Render

O `render.yaml` na raiz já descreve o serviço (Docker, plano free, health check em
`/actuator/health`, `autoDeploy` desligado). No painel: **New → Blueprint**, aponte para o
repositório e preencha as variáveis marcadas como `sync: false` — as três do banco,
`GOOGLE_CLIENT_ID` e `CORS_ALLOWED_ORIGINS` com a origem do frontend. O `JWT_SECRET` o próprio
Render gera.

### 3. Ligar o CI ao Render

Em **Settings → Deploy Hook** do serviço, copie a URL e cadastre no GitHub em
**Settings → Secrets and variables → Actions** com o nome `RENDER_DEPLOY_HOOK`.

Sem esse secret, o workflow ainda roda os testes e passa — apenas registra no log que nenhum
deploy foi disparado, em vez de falhar.

### Sobre o free tier

O serviço hiberna após inatividade, então a primeira requisição depois de um tempo parado leva
alguns segundos. O `-XX:MaxRAMPercentage=75` no Dockerfile existe porque, sem ele, a JVM calcula
o heap errado nos 512 MB do plano gratuito.

## Status

Blocos 1 a 6 concluídos: ambiente em Docker, tratamento de erro centralizado, CORS,
autenticação (registro, login por senha e login com Google, todos emitindo JWT próprio),
cadastro de categorias e contas/cartões, lançamentos com filtros combináveis e confirmação, e
os lançamentos parcelados e recorrentes, o calendário mensal com alertas de vencimento e a
visão geral com saldo e projeção dos próximos meses, além da previsão com cenários e do fluxo
de caixa, os investimentos com meta de aporte mensal e as metas financeiras, além do pipeline
de CI e da configuração de deploy. Todos os 12 blocos do roadmap estão concluídos.