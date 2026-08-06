# Stockify

Aplicação Android de gestão de stock para armazém, com autenticação local e base de dados no dispositivo.

## Funcionalidades

- Registo e login de utilizador com password derivada com PBKDF2 e sal
- Sessão persistente entre arranques da aplicação
- Dashboard com stock total, número de produtos, alertas e valor em stock
- Listagem de produtos com pesquisa por nome ou SKU e filtro por categoria
- CRUD completo de produtos: criar, ler, editar e eliminar
- Entradas e saídas de stock com histórico de movimentos
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

Na primeira execução são inseridos cinco produtos de exemplo e as contas de
demonstração do enunciado: `admin` / `password123`, `cesae` / `cesae` e
`diana` / `diana123`.

## Testes

```
./gradlew test                  # lógica pura, corre na máquina, sem emulador
./gradlew connectedAndroidTest  # movimentos de stock, precisa de emulador
```

Os testes de unidade cobrem a derivação e validação de passwords, os filtros e
ordenações da lista de produtos e as contas do dashboard. O teste instrumentado
verifica que um movimento de stock nunca deixa quantidades negativas nem grava
um movimento sem alterar o produto.

## Limitações conhecidas

- **Contas de demonstração em claro no código.** São as do enunciado e têm de
  funcionar na aplicação entregue. Numa aplicação real seriam geradas no
  primeiro arranque, com mudança de password obrigatória no primeiro login.
- **Base de dados sem encriptação.** Só é legível com acesso físico ao
  dispositivo e root; encriptá-la exigiria SQLCipher. As passwords estão
  protegidas com PBKDF2 e sal, portanto o ficheiro não é uma lista de
  passwords.
- **Sem migrações Room.** Uma mudança de versão do schema recria a base de
  dados e os dados locais perdem-se.
- **Release sem R8.** Ligar o `optimization.enable` no AGP 9.2 exige uma flag
  experimental do Gradle; ficou por ligar até essa flag estabilizar.
- A autenticação é local, sem servidor. Os dados existem apenas no dispositivo,
  e o backup automático do Android está desligado para que não saiam dele.
- Encomendas e leitura de código de barras ficaram fora do âmbito por
  restrição de prazo.
