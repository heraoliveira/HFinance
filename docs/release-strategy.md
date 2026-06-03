# Estratégia de Release

O HFinance segue uma linha 1.x estável para usuários da aplicação desktop local.

## Branches

- `main`: desenvolvimento normal e releases aprovadas.
- `release/1.x`: correções seguras para usuários atuais.
- `develop`: opcional para mudanças maiores.

Se a branch `release/1.x` ainda não existir:

```bash
git checkout main
git pull
git checkout -b release/1.x
git push -u origin release/1.x
```

Hotfixes devem partir da linha estável:

```bash
git checkout release/1.x
git checkout -b hotfix/1.1.x-descricao-curta
```

## Versionamento

- `1.0.x`: correções de bug.
- `1.1.x`: melhorias pequenas sem quebrar dados.
- `1.2.x`: melhorias maiores, ainda desktop local.
- `2.0.0`: mudança estrutural relevante, como API obrigatória ou arquitetura nova.

## Regras da Linha 1.x

- preservar dados locais;
- manter Java 17, JavaFX, SQLite e funcionamento offline;
- não introduzir Spring Boot, PostgreSQL, API obrigatória, login, nuvem ou Android;
- não alterar migrations antigas já publicadas;
- criar backup antes de migrations em banco existente;
- entregar release com ZIP portátil e instalador Windows, não apenas source code.

## Checklist de Release

1. Atualizar versão e changelog.
2. Executar `mvn test`.
3. Executar `mvn package`.
4. Executar `.\scripts\package-windows.ps1`.
5. Validar `target/package/HFinance/HFinance.exe`.
6. Validar `target/release/HFinance-vX.Y.Z-windows.zip`.
7. Validar `target/release/HFinance-Setup-vX.Y.Z.exe`.
8. Abrir PR para `main`.
9. Fazer squash merge após checks.
10. Atualizar `release/1.x`.
11. Criar tag anotada.
12. Publicar release GitHub com ZIP portátil e instalador Windows.
