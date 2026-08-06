# StockAki

Aplicação Android de gestão de stock para armazém. Autenticação local, base de
dados no dispositivo, sem servidor e sem ligação à rede.

---

## Ecrãs

| Ecrã | O que faz |
|---|---|
| Entrada | Apresentação da app; salta direto para o painel se já houver sessão |
| Login | Entrada com utilizador e password |
| Registo | Criação de conta com validação de nome, email e password |
| Painel | Indicadores do armazém, gráfico por categoria, top produtos e últimos movimentos |
| Inventário | Lista de produtos com pesquisa, filtros, ordenação e separador de alertas |
| Formulário de produto | Criação e edição, com fotografia |
| Histórico de movimentos | Todas as entradas e saídas registadas |
| Perfil | Resumo do armazém e dados da conta |
| Editar perfil | Alteração de nome e password |

---

## Funcionalidades

### Conta e sessão

- Registo de utilizador com validação de nome, formato de email
  (`Patterns.EMAIL_ADDRESS`) e password com mínimo de seis caracteres
- Login com mensagens de erro no próprio campo, visíveis enquanto o utilizador
  corrige e anunciadas pelo leitor de ecrã
- Password derivada com PBKDF2, sal aleatório por conta e 210 000 iterações
- Sessão persistente entre arranques da aplicação
- Alteração de nome e de password no perfil, exigindo a password atual
- Terminar sessão com confirmação, a partir do perfil ou do menu lateral

### Painel

- Stock total, valor do stock, lucro total e número de alertas
- Gráfico de anel com a receita repartida por categoria, desenhado à mão numa
  `View` própria, sem bibliotecas de gráficos
- Top três produtos por receita, com barra de proporção
- Últimos dez movimentos de stock
- Saudação conforme a hora do dia e contador de alertas no cabeçalho

### Produtos

- Criar, ler, editar e eliminar, com confirmação antes de eliminar
- Fotografia do produto escolhida da galeria e copiada para o
  armazenamento interno da app
- Pesquisa por nome ou SKU, indiferente a maiúsculas
- Filtro por categoria através de chips
- Ordenação por nome, por quantidade crescente ou por valor em stock decrescente
- SKU com índice único: dois produtos não podem partilhar o mesmo código
- Rascunho do formulário guardado se o utilizador retroceder sem gravar,
  limpo ao terminar sessão
- Estado (OK, Baixo, Esgotado) calculado a partir da quantidade e do stock
  mínimo, nunca guardado, para não haver dados contraditórios

### Movimentos de stock

- Registo de entradas e saídas com quantidade, motivo e autor
- Ajuste do stock e gravação do movimento numa única transação: saídas
  simultâneas não se perdem e o stock nunca fica negativo
- Recusa de saídas superiores ao stock, com a quantidade real na mensagem
- Ecrã de histórico completo, além dos últimos movimentos no painel

### Alertas

- Separador próprio no inventário com os produtos abaixo do stock mínimo
- Contador de alertas no cabeçalho do painel

### Exportação

- Exportação do inventário para CSV ou para folha de cálculo do Excel
- Exporta o que está visível, respeitando a pesquisa e os filtros ativos
- Destino escolhido pelo utilizador através do seletor do sistema
  (Storage Access Framework), sem pedir permissões de armazenamento
- CSV com marca de ordem de bytes, separador `;` e vírgula decimal, para abrir
  corretamente no Excel português

### Navegação

- Barra inferior com Painel, Inventário e Perfil
- Menu lateral, aberto pelo botão ☰ dos três ecrãs principais, com editar
  perfil, histórico de movimentos, sobre e terminar sessão

### Acessibilidade

- Descrições de conteúdo em todos os botões só com ícone
- Erros marcados no campo que os causou, em vez de mensagens passageiras
- Quantidades de movimento anunciadas por extenso ("Entrada de 5"), porque o
  sinal e a cor sozinhos não chegam ao leitor de ecrã
- Cores de estado escurecidas para cumprir o contraste mínimo de 4,5:1

---

## Tecnologias

- Kotlin
- Android SDK, minSdk 24 (Android 7.0), targetSdk 36
- Layouts XML com ViewBinding
- Room para persistência local, com KSP
- Navigation Component, BottomNavigationView e DrawerLayout
- ViewModel, LiveData e coroutines
- Material 3
- Storage Access Framework para gravar os ficheiros exportados
- PBKDF2 do próprio JDK para as passwords

Sem bibliotecas de gráficos, de imagem ou de injeção de dependências.

---

## Arquitetura

```
com.example.gesto_stocks
├── data
│   ├── local     Base de dados Room e DAOs
│   └── model     Produto, Utilizador, Movimento
├── ui
│   ├── inicio    Painel e gráfico de anel
│   ├── stocks    Inventário, formulário e movimentos de stock
│   ├── alertas   Adapter dos produtos em alerta
│   ├── movimentos Histórico completo
│   └── Perfil    Perfil e edição de perfil
└── util          Sessão, segurança, imagens, exportação e formatação
```

Um package por ecrã, cada um com o seu Fragment e, quando precisa de estado, o
seu ViewModel. As contas do painel e os filtros do inventário são funções puras
de topo, sem dependências do Android, para poderem ser testadas sem emulador.

---

## Segurança

- Passwords com PBKDF2, sal por conta e comparação em tempo constante
- Backup automático do Android desligado, tanto para a cloud como para a
  transferência entre dispositivos: a base de dados não sai do aparelho
- Todas as consultas passam pelo Room e são parametrizadas
- Sem permissões perigosas no manifesto; só o ecrã de entrada é exportado

O relatório completo da auditoria de segurança está em
[RELATORIO_AUDITORIA.md](RELATORIO_AUDITORIA.md).

---

## Como executar

1. Clonar o repositório
2. Abrir no Android Studio
3. Aguardar a sincronização do Gradle
4. Executar num emulador ou dispositivo com Android 7.0 ou superior

Na primeira execução são inseridos cinco produtos de exemplo e as contas de
demonstração do enunciado:

| Utilizador | Password |
|---|---|
| `admin` | `password123` |
| `cesae` | `cesae` |
| `diana` | `diana123` |

---

## Testes

```
./gradlew test                  # 18 testes, corre na máquina, sem emulador
./gradlew connectedAndroidTest  # 4 testes, precisa de emulador
```

| Suite | Cobre |
|---|---|
| `SegurancaTest` | Derivação e validação de passwords, incluindo hashes antigos ou corrompidos |
| `FiltrosTest` | Pesquisa, filtro por categoria e as três ordenações |
| `EstatisticasTest` | As seis contas do painel, incluindo lucro negativo e lista vazia |
| `MovimentoDaoTest` | Movimentos sobre base em memória: dez saídas simultâneas sobre stock de cinco têm de dar exatamente cinco sucessos |

---

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
- **Sem limite de tentativas de login e sem expiração de sessão.** A aplicação
  é local e não tem rede, pelo que um ataque exigiria acesso físico continuado.
- A exportação para Excel gera a folha em XML do Excel 2003, que o Excel e o
  LibreOffice abrem, mas não é o formato `.xlsx` moderno.
- Encomendas e leitura de código de barras ficaram fora do âmbito por
  restrição de prazo.
