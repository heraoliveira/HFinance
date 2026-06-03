# Empacotamento Windows

O empacotamento usa `jpackage` disponível no JDK 17.

## Pré-Requisitos

- Windows.
- JDK 17 no `PATH`.
- Maven no `PATH`.
- `jpackage --version` retornando versão 17.
- WiX opcional para gerar instalador `.exe`.

## Comando

```powershell
.\scripts\package-windows.ps1
```

O script executa:

1. Validação do Java 17.
2. Validação do Maven.
3. Validação do `jpackage`.
4. `mvn clean package`.
5. Cópia das dependências de runtime.
6. Execução do `jpackage` com `--type app-image`.
7. Tentativa de gerar instalador `.exe` quando WiX está disponível.

## Saída Esperada

Imagem da aplicação:

```text
target/package/HFinance/HFinance.exe
```

Instalador, quando WiX estiver disponível:

```text
target/package/HFinance-1.1.0.exe
```

Se o WiX não estiver instalado, o script informa que o instalador não foi gerado. Nesse caso, a imagem da aplicação pode ser distribuída como pasta compactada.

## Dados do Usuário

O pacote não usa a pasta de instalação como fonte de verdade para dados persistentes. O diretório oficial no Windows é:

```text
%APPDATA%/HFinance
```

Ao desinstalar o HFinance, os dados financeiros locais devem permanecer no computador, salvo remoção manual explícita pelo usuário.

## Release ZIP

Para release GitHub, compacte a pasta completa:

```text
target/package/HFinance
```

O arquivo de distribuição recomendado é:

```text
target/release/HFinance-v1.1.0-windows.zip
```

Esse ZIP deve conter `HFinance.exe`, runtime, dependências e recursos necessários para execução.
