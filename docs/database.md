# Banco de dados

O HFinance usa SQLite local com migrations Flyway.

## Local do banco

Local preferencial:

```text
data/hfinance.db
```

Fallback:

```text
%APPDATA%/HFinance/hfinance.db
```

## Migrations

- `V1__create_initial_schema.sql`: cria tabelas e índices.
- `V2__insert_default_categories.sql`: insere categorias padrão.

## Tabelas

- `accounts`: contas financeiras.
- `categories`: categorias padrão de receita e despesa.
- `transactions`: receitas e despesas.
- `budgets`: limites mensais por categoria de despesa.
- `goals`: metas financeiras.

## Persistência

Repositories JDBC usam `PreparedStatement` e `try-with-resources`. Chaves estrangeiras são habilitadas com `PRAGMA foreign_keys = ON` em cada conexão.
