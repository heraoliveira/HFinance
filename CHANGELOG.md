# Changelog

Todas as mudanças relevantes do HFinance são documentadas neste arquivo.

## [1.1.1] - 2026-06-03

### Corrigido

- Corrigida inconsistência de versão entre release, aplicação e `pom.xml`.
- Corrigida documentação do local oficial do banco de dados.
- Alinhados README, CHANGELOG e documentação técnica sobre `%APPDATA%/HFinance`.
- Corrigido empacotamento Windows para gerar instalador com WiX.

### Empacotamento

- Adicionado instalador Windows `.exe` gerado via `jpackage` com WiX.
- Mantido ZIP portátil para distribuição alternativa.
- Validado que a desinstalação não remove dados financeiros do usuário.

### Segurança de dados

- Reforçada documentação de que `data/hfinance.db` é apenas banco legado.
- Reforçado que `%APPDATA%/HFinance` é o diretório oficial de dados.

## [1.1.0] - 2026-06-03

### Adicionado

- Diretório oficial de dados em `%APPDATA%/HFinance`.
- Backup automático antes de migrations pendentes em banco existente.
- Backup manual pela interface.
- Área **Sobre** com informações técnicas, pastas de dados, backups, logs e exports.
- Logs locais em `hfinance.log`, log diário e `hfinance-error.log`.
- Exportação de diagnóstico técnico sem dados financeiros sensíveis intencionais.
- Tratamento global de erros inesperados com mensagem amigável.
- Ícone da aplicação em ciano e dourado para JavaFX e empacotamento Windows.
- Testes de regressão para backup, integridade do banco, migration com backup e migração de banco legado.

### Alterado

- Versão do projeto atualizada para `1.1.0`.
- Inicialização do banco agora prioriza o diretório de dados do usuário.
- Mensagens de erro críticas foram revisadas para português brasileiro claro.
- Empacotamento Windows foi fortalecido com validação de Java 17, Maven, `jpackage`, ícone, versão e tentativa de instalador via WiX.
- Exportações de relatórios usam `%APPDATA%/HFinance/exports` como pasta padrão.

### Segurança de dados

- Banco legado `data/hfinance.db` é migrado com backup prévio e validação.
- Banco legado é preservado e nunca removido automaticamente.
- Migrations não rodam sem validação prévia em banco existente.
- Banco corrompido ou inacessível não é aberto silenciosamente.
- Backups automáticos mantêm retenção padrão dos últimos 10 arquivos e preservam backups manuais.

### Não incluído

- Spring Boot.
- PostgreSQL.
- Login.
- Nuvem.
- Android.
- Hibernate ou JPA.
