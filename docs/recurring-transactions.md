# Transações Recorrentes

A recorrência da versão 1.2.1 é simples e explícita: as ocorrências futuras são criadas no momento do cadastro.

## Tipos

- Não repetir.
- Semanalmente.
- Mensalmente.
- Anualmente.

## Regras

- Recorrência é opcional.
- O usuário informa a quantidade de repetições adicionais.
- A quantidade mínima é 1 repetição adicional.
- A quantidade máxima é 120 repetições adicionais.
- Uma transação mensal com quantidade 3 gera 4 transações no total: a original e 3 repetições.
- Datas mensais e anuais usam `LocalDate.plusMonths` e `LocalDate.plusYears`, preservando o comportamento seguro do Java para meses menores.
- Relatórios consideram transações futuras apenas quando o filtro inclui essas datas.

## Edição

Ao editar uma transação recorrente, a aplicação exibe confirmação interna com três opções:

- Alterar somente esta transação.
- Alterar esta e as próximas.
- Cancelar.

Ao escolher **Alterar esta e as próximas**, a ocorrência selecionada e as futuras do mesmo `recurrence_group_id` recebem conta, categoria, tipo, método de pagamento, descrição e valor atualizados. O histórico anterior permanece intacto. A data da ocorrência selecionada pode ser alterada; as datas futuras permanecem nas datas originais da série.

Excluir uma transação recorrente remove somente a ocorrência selecionada.

## Banco

A migration `V3__quality_usability_1_2.sql` adiciona à tabela `transactions`:

- `recurrence_group_id`
- `recurrence_type`
- `recurrence_index`
- `recurrence_total`

A migration `V4__hotfix_1_2_1.sql` adiciona índice por `recurrence_group_id` e `transaction_date` para atualização segura da série futura.
