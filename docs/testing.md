# Testes

Os testes usam JUnit 5, AssertJ e bancos SQLite temporários com migrations reais.

## Executar

```powershell
mvn test
```

Para validar compilação e empacotamento Maven:

```powershell
mvn package
```

## Cobertura Atual

- Regras de domínio: valor positivo, status de orçamento, progresso e status de meta, limite da descrição.
- Services: contas, duplicidade, inativação, receitas, despesas, saldo, transações, orçamentos e metas.
- Repositories: persistência e filtros de transações.
- Relatórios: Excel, abas obrigatórias, CSV, cabeçalhos e relatório vazio.
- Segurança de banco: backup validado, banco inválido rejeitado, backup antes de migration pendente e migração segura de banco legado.

## Regras Protegidas

- Valores monetários usam `BigDecimal`.
- Datas usam `LocalDate` e `LocalDateTime`.
- Conta ativa tem nome único.
- Conta inativa não recebe novas transações.
- Orçamento só aceita categoria de despesa.
- Meta calcula status automaticamente.
- Relatório vazio não exporta sem aviso.
- Banco corrompido não é tratado como saudável.
- Banco legado é copiado somente após backup e validação.

## JaCoCo

O relatório é gerado em:

```text
target/site/jacoco
```
