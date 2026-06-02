# Arquitetura

O HFinance usa arquitetura em camadas para manter regra de negócio fora da interface e SQL fora dos controllers.

## Camadas

- `core`: configuração, conexão com banco, migrations, exceções e formatadores.
- `domain`: entidades, enums e regras puras.
- `application`: services e DTOs usados pela UI e pelos relatórios.
- `infrastructure`: repositories JDBC e exportadores Excel/CSV.
- `ui`: aplicação JavaFX, navegação, controllers, componentes e viewmodels.

## Fluxo

1. Controllers JavaFX recebem eventos da interface.
2. Controllers chamam services.
3. Services validam regras e chamam repositories.
4. Repositories executam SQL com `PreparedStatement`.
5. Services retornam DTOs para a UI.

## Decisões

- A interface foi criada programaticamente em JavaFX, com CSS externo, para reduzir dependência de FXML e manter tipagem direta.
- O banco é SQLite local, inicializado por Flyway.
- Valores monetários usam `BigDecimal`.
- O saldo atual é sempre calculado e não armazenado como fonte de verdade.
