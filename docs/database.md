# Banco de Dados

O HFinance usa SQLite local com migrations Flyway.

## Local Oficial

No Windows, o banco oficial do usuário final fica em:

```text
%APPDATA%/HFinance/hfinance.db
```

A pasta completa criada pela aplicação é:

```text
%APPDATA%/HFinance/
├── hfinance.db
├── backups/
├── logs/
├── exports/
└── config.properties
```

Em ambientes não Windows, o fallback seguro fica no perfil do usuário, como `~/.hfinance`.

## Migração do Banco Legado

Versões anteriores podiam usar o caminho legado:

```text
data/hfinance.db
```

Na inicialização, a aplicação segue este fluxo:

1. A aplicação procura o banco oficial em `%APPDATA%/HFinance/hfinance.db`.
2. Detecta banco legado e banco oficial.
3. Se só existir banco oficial, valida e usa o oficial.
4. Se só existir banco legado em `data/hfinance.db`, valida o legado, cria backup automático, copia para o local oficial, valida a cópia e usa o oficial.
5. Se ambos existirem, usa o oficial e preserva o legado.

O banco antigo `data/hfinance.db` não é apagado automaticamente.

## Validação de Integridade

Antes de usar banco existente, o HFinance valida:

- existência do arquivo;
- permissão de leitura;
- abertura de conexão JDBC;
- `PRAGMA integrity_check`;
- versão do schema registrada pelo Flyway, quando disponível.

Se a validação falhar, migrations não são executadas e a inicialização exibe uma mensagem segura ao usuário.

## Migrations

- `V1__create_initial_schema.sql`: cria tabelas e índices.
- `V2__insert_default_categories.sql`: insere categorias padrão.

Regras:

- migrations antigas publicadas não devem ser alteradas;
- toda mudança de schema deve ser uma nova migration;
- antes de migrations pendentes em banco existente, a aplicação cria backup automático;
- falha de migration não deve ser mascarada como sucesso.

## Tabelas

- `accounts`: contas financeiras.
- `categories`: categorias padrão de receita e despesa.
- `transactions`: receitas e despesas.
- `budgets`: limites mensais por categoria de despesa.
- `goals`: metas financeiras.

## Persistência

Repositories JDBC usam `PreparedStatement` e `try-with-resources`. Chaves estrangeiras são habilitadas com `PRAGMA foreign_keys = ON` em cada conexão.
