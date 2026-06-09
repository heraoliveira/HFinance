# Armazenamento de Dados

O HFinance é uma aplicação local e offline. Dados financeiros ficam no computador do usuário.

## Estrutura Oficial no Windows

```text
%APPDATA%/HFinance/
├── hfinance.db
├── backups/
├── logs/
├── exports/
└── config.properties
```

Esse diretório é criado automaticamente quando a aplicação inicia.

## Configuração

O arquivo `config.properties` é recriado com padrões seguros quando ausente:

```properties
app.version=1.2.2
database.path=%APPDATA%/HFinance/hfinance.db
backup.retention.maxAutomaticBackups=10
diagnostics.includeLogTail=true
```

O arquivo não contém segredos.

## Banco Legado

A aplicação procura primeiro o banco oficial em `%APPDATA%/HFinance/hfinance.db`.

Se existir apenas `data/hfinance.db`, a aplicação trata esse arquivo como banco legado e:

1. valida o banco legado;
2. cria backup automático;
3. copia para `%APPDATA%/HFinance/hfinance.db`;
4. valida a cópia;
5. usa o banco novo;
6. mantém o banco legado no local original.

Se o banco oficial já existir, ele é usado como fonte de verdade e o legado é apenas preservado.

## Exportações

Exportações técnicas e relatórios usam `%APPDATA%/HFinance/exports` como pasta padrão quando a UI não define outro local.
