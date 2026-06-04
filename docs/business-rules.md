# Regras de negócio

## Contas

- Nome é obrigatório.
- Conta ativa deve ter nome único.
- Saldo inicial é obrigatório.
- Conta inativa não recebe novas transações.
- Conta com transações não pode ser excluída fisicamente pela ação de exclusão; deve ser inativada.
- Saldo atual é calculado por saldo inicial + receitas - despesas.

## Transações

- Conta, categoria, data, tipo, método de pagamento e valor são obrigatórios.
- Valor deve ser maior que zero.
- Descrição pode ser vazia e tem limite de 255 caracteres.
- Receita aumenta saldo.
- Despesa diminui saldo.
- Transações futuras são permitidas.
- Transação recorrente é opcional e gera ocorrências individuais no momento do cadastro.
- Recorrência aceita semanal, mensal e anual.
- Recorrência deve ter quantidade ou data final, sem recorrência infinita.
- O limite seguro é de 120 ocorrências.
- Editar ou excluir uma transação recorrente altera apenas a ocorrência selecionada na versão 1.2.0.

## Categorias

- Categorias padrão são inseridas na primeira execução.
- Categorias personalizadas podem ser criadas, editadas, inativadas e excluídas quando nunca foram usadas.
- Nome ativo deve ser único por tipo, ignorando maiúsculas, minúsculas e espaços extras.
- Categoria padrão não é excluída fisicamente.
- Categoria usada em transações ou orçamentos deve ser inativada em vez de excluída.
- Categoria inativa não aparece como opção padrão em novos cadastros.
- Transação deve usar categoria do mesmo tipo da transação.
- Orçamento só aceita categoria de despesa ativa.

## Orçamentos

- Um orçamento pertence a uma categoria de despesa.
- Valor limite deve ser maior que zero.
- Não é permitido duplicar categoria, mês e ano.
- Uso é calculado por gasto / limite * 100.
- Status: dentro do limite abaixo de 80%, atenção entre 80% e 100%, excedido acima de 100%.

## Metas

- Nome, valor alvo, valor atual e prazo são obrigatórios.
- Valor alvo deve ser maior que zero.
- Valor atual não pode ser negativo.
- Progresso é calculado por valor atual / valor alvo * 100.
- Status é concluída, atrasada ou em andamento conforme valor e prazo.
