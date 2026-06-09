# Changelog

Todas as mudanças relevantes do HFinance são documentadas neste arquivo.

## [1.2.2] - 2026-06-09

### Corrigido

- Corrigido o dimensionamento dos toasts para manter altura baseada no conteúdo e empilhamento no canto superior direito.
- Corrigido o dimensionamento dos modais internos, com ações responsivas e suporte a Enter e Esc.
- Adicionados estados de sucesso, erro, alerta e informação às notificações integradas.
- Direcionados erros inesperados para a interface integrada quando a janela principal já está disponível.
- Adicionadas confirmações antes de inativar contas e categorias.
- Completados os gráficos da Visão Geral e Relatórios com títulos, eixos, legendas, tooltips, valores em reais, percentuais e estados vazios.
- Sincronizada a versão `1.2.2` no Maven, aplicação, documentação e empacotamento Windows.

## [1.2.1] - 2026-06-05

### Corrigido

- Sincronizada a versão `1.2.1` no Maven, aplicação e empacotamento Windows.
- Corrigido o ZIP portátil para não expor `HFinance.ico` solto ao lado de `HFinance.exe`.
- Removidos botões redundantes de novo cadastro nos formulários de contas, transações, categorias, orçamentos e metas.
- Substituídos alertas comuns de sucesso, erro e confirmação por notificações e modais internos na janela do HFinance.
- Removido o campo `Data final` da recorrência de transações.
- Ajustada a quantidade de recorrência para representar repetições adicionais, com limite de 1 a 120.
- Adicionada confirmação interna para editar somente a transação recorrente selecionada ou a selecionada e as próximas da série.
- Permitida a exclusão física de categorias padrão sem uso e bloqueada a exclusão de categorias usadas com orientação para inativação.
- Removidas as categorias padrão `Freelance` e `Presente` em novos bancos e inativadas em bancos existentes quando já estiverem em uso.
- Substituída a coluna textual de cor por amostra visual na tabela de categorias.
- Melhorada a resposta de rolagem nas telas com conteúdo vertical.

## [1.2.0] - 2026-06-04

### Adicionado

- CRUD de categorias personalizadas com filtro por tipo, cor, origem e status.
- Suporte a categorias inativas, mantendo histórico visível em transações e relatórios.
- Criação de transações recorrentes semanalmente, mensalmente ou anualmente, com limite seguro de 120 ocorrências.
- Filtro de categoria e atalhos de período na tela de transações.
- Filtros preservados na sessão para transações e relatórios.
- Cards e gráficos adicionais em relatórios, incluindo receitas vs despesas por mês, despesas por categoria e métodos de pagamento.
- Gráfico de despesas por categoria na visão geral.
- Novo ícone em ciano e dourado com H maior para JavaFX e empacotamento Windows.
- Migration `V3__quality_usability_1_2.sql` para categorias ativas, recorrência e índices de filtros.

### Alterado

- Versão atualizada para `1.2.0` no Maven, aplicação e script de empacotamento.
- Formulários de transações, contas, orçamentos e metas agora preservam dados úteis para cadastros repetitivos.
- Relatórios exportados usam nomes com timestamp e mantêm o diretório padrão `%APPDATA%/HFinance/exports`.
- Exportação Excel inclui metadado de geração, cabeçalho congelado e autofiltro nas abas.
- Orçamentos passam a aceitar apenas categorias de despesa ativas em novos cadastros.
- Tabelas e telas seguem layout rolável e com altura controlada para reduzir espaços vazios e cortes.

### Corrigido

- DatePicker estilizado com mês e ano legíveis, contraste adequado e alinhamento visual com a paleta ciano/dourado.
- CSV passa a escapar quebras de linha `\r` além de aspas, ponto e vírgula e `\n`.
- Mensagens e textos visíveis revisados em português brasileiro correto nos fluxos alterados.

### Não incluído

- Spring Boot.
- PostgreSQL.
- Login.
- Nuvem.
- Android.
- Hibernate ou JPA.

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
