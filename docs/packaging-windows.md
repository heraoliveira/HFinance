# Empacotamento Windows

O empacotamento usa `jpackage` disponível no JDK 17.

## Pré-requisitos

- Windows.
- JDK 17 no `PATH`.
- Maven no `PATH`.
- `jpackage --version` retornando versão 17.

## Comando

```powershell
.\scripts\package-windows.ps1
```

O script executa:

1. Validação do Java 17.
2. `mvn -DskipTests package`.
3. Cópia das dependências de runtime.
4. Execução do `jpackage` com `--type app-image`.

## Saída esperada

```text
target/package/HFinance/HFinance.exe
```

Se o ambiente não permitir gerar a imagem de aplicativo, o script informará o erro. Não considere o executável gerado sem validar a existência do arquivo acima.
