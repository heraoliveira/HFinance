# Empacotamento Windows

O empacotamento usa `jpackage` do JDK 17 e WiX para gerar a aplicação Windows.

## Pré-Requisitos

- Windows.
- JDK 17 no `PATH`.
- Maven no `PATH`.
- `jpackage` do JDK 17 no `PATH`.
- WiX Toolset no `PATH`.

Valide o ambiente:

```powershell
java -version
mvn -version
jpackage --version
wix --version
```

Se a instalação local do WiX expuser apenas `candle.exe` e `light.exe`, o script também aceita esses comandos. O ambiente de release validado usa `wix --version`.

## Comando

```powershell
.\scripts\package-windows.ps1
```

O script executa:

1. Validação do Java 17.
2. Validação do Maven.
3. Validação do `jpackage`.
4. Validação do WiX e exibição da versão encontrada.
5. `mvn clean package`.
6. Cópia das dependências de runtime.
7. Execução do `jpackage` com `--type app-image`.
8. Geração do ZIP portátil.
9. Execução do `jpackage` com `--type exe` para gerar o instalador Windows.

Se o WiX não for encontrado, o script falha com mensagem clara. A release `1.1.1` não é considerada pronta sem o instalador `.exe`.

## Saídas

Imagem da aplicação:

```text
target/package/HFinance/HFinance.exe
```

ZIP portátil:

```text
target/release/HFinance-v1.1.1-windows.zip
```

Instalador Windows:

```text
target/release/HFinance-Setup-v1.1.1.exe
```

O ZIP contém `HFinance.exe`, `runtime/`, `app/` e recursos necessários para execução. O instalador integra o HFinance ao Windows, cria atalho no Menu Iniciar, usa o ícone correto e exibe a versão `1.1.1`.

## Dados do Usuário

O pacote não usa a pasta de instalação como fonte de verdade para dados persistentes. O diretório oficial no Windows é:

```text
%APPDATA%/HFinance
```

Ao desinstalar o HFinance, os dados financeiros locais devem permanecer no computador, salvo remoção manual explícita pelo usuário. O instalador não deve apagar `hfinance.db`, `backups/`, `logs/` ou `exports/`.
