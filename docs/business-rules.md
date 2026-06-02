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

## Categorias

- Categorias padrão são inseridas na primeira execução.
- Transação deve usar categoria do mesmo tipo da transação.
- Orçamento só aceita categoria de despesa.

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
