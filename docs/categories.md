# Categorias

A versão 1.2.2 mantém categorias personalizadas e as regras de exclusão da linha desktop local do HFinance.

## Regras

- Categorias têm nome, tipo, cor, origem e status.
- Tipo é obrigatório: receita ou despesa.
- Nome ativo é único por tipo, ignorando maiúsculas, minúsculas e espaços extras.
- Categorias padrão e personalizadas sem uso podem ser excluídas fisicamente.
- Categorias usadas em transações ou orçamentos não são excluídas fisicamente; elas devem ser inativadas.
- Categorias inativas continuam aparecendo em históricos, relatórios e filtros quando já existem dados.
- Novos cadastros usam apenas categorias ativas.
- As categorias padrão `Freelance` e `Presente` não são mantidas em bancos novos.

## Banco

A migration `V3__quality_usability_1_2.sql` adiciona:

- `categories.is_active`
- `categories.updated_at`
- índice único parcial para nome ativo por tipo
- índice por tipo e status

A migration `V4__hotfix_1_2_1.sql` remove `Freelance` e `Presente` quando não há uso. Em bancos existentes, se alguma delas já estiver referenciada por transações ou orçamentos, a categoria é preservada e marcada como inativa.

## Interface

A tela **Categorias** permite listar, filtrar, criar, editar, inativar, reativar e excluir categorias quando permitido. A tabela mostra a cor como amostra visual ao lado do nome, sem coluna textual de cor.
