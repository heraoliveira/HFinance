# Logs e Diagnóstico

Logs locais ficam em:

```text
%APPDATA%/HFinance/logs
```

Arquivos principais:

```text
hfinance.log
hfinance-YYYY-MM-DD.log
hfinance-error.log
```

## Conteúdo dos Logs

Os logs registram:

- versão do HFinance;
- data e hora;
- sistema operacional;
- versão do Java;
- caminho do banco;
- diretório oficial de dados;
- resultado de validação do banco;
- eventos de backup;
- execução e falhas de migrations;
- exceções completas.

Dados financeiros sensíveis não devem ser registrados sem necessidade técnica clara.

## Tratamento de Erros

Erros inesperados exibem mensagem amigável:

```text
Ocorreu um erro inesperado.
```

Stack trace completo vai para o log. O usuário não vê stack trace cru.

## Diagnóstico

A tela **Sobre** permite exportar um diagnóstico para:

```text
%APPDATA%/HFinance/exports
```

O arquivo contém:

- nome da aplicação;
- versão;
- data/hora;
- sistema operacional;
- versão Java;
- caminho do banco;
- existência e integridade do banco;
- versão do schema;
- último backup;
- quantidade de backups;
- caminho dos logs;
- últimas linhas do log, quando disponíveis.

Dados financeiros sensíveis não são exportados intencionalmente.
