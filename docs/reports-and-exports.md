# Relatórios e Exportações

A versão 1.2.0 melhora relatórios e exportações mantendo a fonte de verdade nos services existentes.

## Relatórios

- Filtros de relatório são preservados durante a sessão.
- Atalhos rápidos cobrem este mês, mês anterior, últimos 30 dias, este ano e ano anterior.
- Cards mostram receitas, despesas, saldo, maior despesa, maior categoria e quantidade de transações.
- Gráficos usam JavaFX Charts para receitas vs despesas por mês, despesas por categoria e métodos de pagamento.
- Relatórios vazios não são exportados sem aviso.

## CSV

- Usa UTF-8.
- Usa separador `;`.
- Mantém cabeçalhos em português brasileiro.
- Escapa aspas, ponto e vírgula, `\n` e `\r`.
- Respeita os filtros ativos.

## Excel

- Contém abas em português brasileiro.
- Inclui aba **Resumo** e aba **Transações**.
- Mantém abas analíticas por mês, categoria, conta, método, orçamentos e metas.
- Congela a linha de cabeçalho.
- Aplica autofiltro.
- Ajusta largura de colunas.
- Inclui metadado de geração na aba **Resumo**.

## Diretório

O diretório padrão continua sendo:

```text
%APPDATA%/HFinance/exports
```
