# Backups

Backups do HFinance ficam em:

```text
%APPDATA%/HFinance/backups
```

## Tipos

- `AUTOMATIC`: criado pela aplicação antes de migrations pendentes e antes da cópia segura do banco legado.
- `MANUAL`: criado pelo usuário na tela **Sobre**.

## Nome dos Arquivos

Formato:

```text
hfinance-backup-2026-06-03-21-30-45.db
```

Se houver colisão no mesmo segundo, a aplicação adiciona sufixo numérico seguro.

## Consistência

Quando possível, o backup usa `VACUUM INTO` do SQLite. Se essa operação não estiver disponível, a aplicação faz cópia de arquivo e valida o resultado com `PRAGMA integrity_check`.

A aplicação só informa sucesso quando o backup foi validado.

## Retenção

A política padrão mantém os últimos 10 backups automáticos. Backups manuais são preservados.

Falhas ao limpar backups antigos são registradas em log e não impedem a criação do backup atual.

## Uso na Interface

Acesse **Sobre > Segurança dos dados** para:

- fazer backup manual;
- abrir a pasta de dados;
- abrir a pasta de backups;
- abrir a pasta de logs.
