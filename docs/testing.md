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
- Services: contas, duplicidade, inativação, receitas, despesas, saldo, transações, recorrência, categorias, orçamentos e metas.
- Repositories: persistência, filtros de transações, recorrência e categorias personalizadas.
- Relatórios: Excel, abas obrigatórias, CSV, cabeçalhos, filtros aplicados e relatório vazio.
- Segurança de banco: backup validado, banco inválido rejeitado, backup antes de migration pendente e migração segura de banco legado.

## Regras Protegidas

- Valores monetários usam `BigDecimal`.
- Datas usam `LocalDate` e `LocalDateTime`.
- Conta ativa tem nome único.
- Conta inativa não recebe novas transações.
- Categorias ativas têm nome único por tipo.
- Categoria usada não é excluída fisicamente.
- Categoria inativa não aparece em novos cadastros.
- Recorrência respeita quantidade, data final e limite de 120 ocorrências.
- Orçamento só aceita categoria de despesa ativa.
- Meta calcula status automaticamente.
- Relatório vazio não exporta sem aviso.
- Banco corrompido não é tratado como saudável.
- Banco legado é copiado somente após backup e validação.

## JaCoCo

O relatório é gerado em:

```text
target/site/jacoco
```
