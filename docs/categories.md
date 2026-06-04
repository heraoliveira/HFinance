# Categorias

A versão 1.2.0 adiciona categorias personalizadas sem alterar a linha desktop local do HFinance.

## Regras

- Categorias têm nome, tipo, cor, origem e status.
- Tipo é obrigatório: receita ou despesa.
- Nome ativo é único por tipo, ignorando maiúsculas, minúsculas e espaços extras.
- Categorias padrão não são excluídas fisicamente.
- Categorias usadas em transações ou orçamentos não são excluídas fisicamente.
- Categorias inativas continuam aparecendo em históricos, relatórios e filtros quando já existem dados.
- Novos cadastros usam apenas categorias ativas.

## Banco

A migration `V3__quality_usability_1_2.sql` adiciona:

- `categories.is_active`
- `categories.updated_at`
- índice único parcial para nome ativo por tipo
- índice por tipo e status

## Interface

A tela **Categorias** permite listar, filtrar, criar, editar, inativar, reativar e excluir categorias quando permitido.
