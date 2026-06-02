# HFinance

[![CI](https://github.com/heraoliveira/HFinance/actions/workflows/ci.yml/badge.svg)](https://github.com/heraoliveira/HFinance/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/heraoliveira/HFinance?label=release)](https://github.com/heraoliveira/HFinance/releases)
![Java 17](https://img.shields.io/badge/Java-17-007396)
![JavaFX 17](https://img.shields.io/badge/JavaFX-17-00A3E0)
![Maven](https://img.shields.io/badge/build-Maven-C71A36)
![SQLite](https://img.shields.io/badge/database-SQLite-044A64)

**Solução desktop para finanças pessoais**

HFinance surge como uma solução desktop local para organização de finanças pessoais, criada para substituir planilhas e controles manuais por uma experiência clara, visual e em português brasileiro. A aplicação centraliza contas, receitas, despesas, orçamentos, metas financeiras e relatórios exportáveis para apoiar o acompanhamento do dinheiro no dia a dia.

## Demonstração

![Demonstração das abas do HFinance](docs/screenshots/hfinance-tabs.gif)

## Funcionalidades

- Cadastro, edição, inativação e exclusão permitida de contas.
- Cadastro, edição, exclusão e filtros de transações.
- Categorias padrão inseridas automaticamente na primeira execução.
- Orçamentos mensais por categoria de despesa, com gasto e status calculados.
- Metas financeiras com progresso e status automático.
- Visão geral com cards, gráfico mensal, últimas transações, contas, alertas e metas.
- Relatórios com filtros e exportação para Excel `.xlsx` e CSV.
- Persistência local em SQLite com migrations Flyway.

## Stack

- Java 17 LTS
- JavaFX 17
- Maven
- SQLite e JDBC
- Flyway
- Apache POI
- JUnit 5, AssertJ, Mockito e JaCoCo
- GitHub Actions
- `jpackage`

## Arquitetura

O projeto usa camadas proporcionais a uma aplicação desktop:

- `domain`: entidades, enums e regras centrais.
- `application`: DTOs e services.
- `infrastructure`: repositories JDBC, migrations e exportadores.
- `ui`: JavaFX, controllers, componentes, navegação e viewmodels.
- `core`: configuração, banco, exceções e formatação.

A interface chama services. Services aplicam regras de negócio. Repositories executam SQL com `PreparedStatement`.

## Banco de dados

O banco SQLite é criado automaticamente em:

```text
data/hfinance.db
```

Se a pasta local não puder ser usada, a aplicação tenta usar:

```text
%APPDATA%/HFinance/hfinance.db
```

As migrations ficam em `src/main/resources/db/migration` e criam as tabelas `accounts`, `categories`, `transactions`, `budgets` e `goals`.

## Regras principais

- Valores monetários usam `BigDecimal`.
- Datas usam `LocalDate` e `LocalDateTime`.
- Conta ativa tem nome único.
- Conta inativa não recebe novas transações.
- Saldo atual não é armazenado; ele é calculado por saldo inicial + receitas - despesas.
- Orçamento só aceita categoria de despesa.
- Meta concluída, atrasada ou em andamento é calculada pelo valor atual e prazo.
- Relatórios vazios não são exportados sem aviso.

## Executar em desenvolvimento

Pré-requisitos:

- JDK 17 no `PATH`
- Maven no `PATH`

Execute:

```powershell
.\scripts\run-dev.ps1
```

Ou diretamente:

```powershell
mvn javafx:run
```

## Gerar o HFinance.exe

O script usa `jpackage` do JDK 17 para gerar uma imagem de aplicativo Windows:

```powershell
.\scripts\package-windows.ps1
```

Quando o ambiente permite o empacotamento, o executável fica em:

```text
target/package/HFinance/HFinance.exe
```

Não afirme que o `.exe` foi gerado em outro ambiente sem executar o script com sucesso.

## Como usar

1. Abra o HFinance em desenvolvimento ou pelo `HFinance.exe`.
2. Cadastre ao menos uma conta em **Contas**.
3. Cadastre receitas e despesas em **Transações**.
4. Acompanhe saldo e evolução em **Visão geral**.
5. Cadastre limites em **Orçamentos**.
6. Cadastre objetivos em **Metas**.
7. Use **Relatórios** para filtrar e exportar dados.

## Testes

Execute:

```powershell
mvn test
```

Para compilar e empacotar o JAR:

```powershell
mvn package
```

O relatório JaCoCo é gerado em `target/site/jacoco`.

## Limitações do MVP

- Não conecta com bancos reais.
- Não importa extrato automaticamente.
- Não possui login.
- Não possui sincronização em nuvem.
- Não possui aplicativo mobile.
- Não substitui software contábil profissional.
- Cartão é apenas método de pagamento no MVP.
- Dados ficam locais no computador do usuário.
- Categorias são padrão no MVP.
- Não há multiusuário.

