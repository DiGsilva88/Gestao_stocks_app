# Stockify

Aplicação Android de gestão de stock para armazém, com autenticação local e base de dados no dispositivo.

## Funcionalidades

- Registo e login de utilizador com password guardada em hash
- Sessão persistente entre arranques da aplicação
- Dashboard com stock total, número de produtos, alertas e valor em stock
- Listagem de produtos com pesquisa por nome ou SKU e filtro por categoria
- CRUD completo de produtos: criar, ler, editar e eliminar
- Alertas automáticos para produtos abaixo do stock mínimo
- Perfil do utilizador com resumo do armazém

## Tecnologias

- Kotlin
- Android SDK, minSdk 24
- Layouts XML com ViewBinding
- Room para persistência local
- Navigation Component com BottomNavigationView
- ViewModel e LiveData
- Material 3

## Arquitetura

Separação em três camadas:

- `data` — entidades, DAOs e base de dados Room
- `ui` — um package por ecrã, cada um com Fragment e ViewModel
- `util` — sessão e funções de segurança

O estado de cada produto (OK, Baixo, Esgotado) é calculado a partir da quantidade
e do stock mínimo, em vez de ser guardado, para evitar dados contraditórios.

## Como executar

1. Clonar o repositório
2. Abrir no Android Studio
3. Aguardar a sincronização do Gradle
4. Executar num emulador ou dispositivo com Android 7.0 ou superior

Na primeira execução são inseridos cinco produtos de exemplo.

## Limitações conhecidas

- A password é guardada com SHA-256 sem sal. Numa aplicação em produção
  usar-se-ia bcrypt ou Argon2.
- A autenticação é local, sem servidor. Os dados existem apenas no dispositivo.
- Histórico de movimentos, encomendas e leitura de código de barras ficaram
  fora do âmbito por restrição de prazo.
