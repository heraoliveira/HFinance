# Testes

Os testes usam JUnit 5, AssertJ e bancos SQLite temporários com migrations reais.

## Executar

```powershell
mvn test
```

## Cobertura atual

- Regras de domínio: valor positivo, status de orçamento, progresso e status de meta, limite da descrição.
- Services: contas, duplicidade, inativação, receitas, despesas, saldo, transações, orçamentos e metas.
- Repositories: persistência e filtros de transações.
- Relatórios: Excel, abas obrigatórias, CSV, cabeçalhos e relatório vazio.

## JaCoCo

O relatório é gerado em:

```text
target/site/jacoco
```
