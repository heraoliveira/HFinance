# Transações Recorrentes

A recorrência da versão 1.2.0 é simples e explícita: as ocorrências futuras são criadas no momento do cadastro.

## Tipos

- Não repetir.
- Semanalmente.
- Mensalmente.
- Anualmente.

## Regras

- Recorrência é opcional.
- O usuário informa quantidade de repetições ou data final.
- A data final deve ser igual ou posterior à data inicial.
- O limite máximo é de 120 ocorrências.
- Se quantidade e data final forem informadas, a opção mais restritiva é usada.
- Datas mensais e anuais usam `LocalDate.plusMonths` e `LocalDate.plusYears`, preservando o comportamento seguro do Java para meses menores.
- Relatórios consideram transações futuras apenas quando o filtro inclui essas datas.

## Edição

Na versão 1.2.0, editar ou excluir uma transação recorrente altera somente a ocorrência selecionada. Não há edição automática da série inteira.

## Banco

A migration `V3__quality_usability_1_2.sql` adiciona à tabela `transactions`:

- `recurrence_group_id`
- `recurrence_type`
- `recurrence_index`
- `recurrence_total`
