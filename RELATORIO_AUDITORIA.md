# Relatório de auditoria de segurança e testes

**Stockify — aplicação Android de gestão de stock**
6 de agosto de 2026

---

## 1. Resumo

Auditoria de segurança e de testes à árvore completa da aplicação, seguida da
correção dos problemas encontrados.

Foram identificados **12 achados de segurança** (3 de severidade alta) e uma
cobertura de testes de **zero por cento** — os dois únicos testes existentes
eram os ficheiros-template gerados pelo Android Studio.

Foram corrigidos **11 dos 12 achados**. O que ficou por corrigir (R8 no build
de release) está justificado na secção 5. A cobertura passou de 2
testes-template para **22 testes reais, todos a passar**.

Nenhuma dependência nova foi acrescentada ao projeto.

---

## 2. Método

Leitura integral do código: 4 Activities, 7 Fragments, 3 ViewModels, 3 DAOs,
3 entidades Room, 2 utilitários, manifesto, ficheiros de build e recursos.

Procura dirigida a padrões de risco conhecidos: SQL concatenado, `WebView`,
`Log` com dados sensíveis, permissões perigosas, componentes exportados,
ficheiros sensíveis em controlo de versões, escritas concorrentes e trabalho
de disco na thread principal.

**Verificado e sem problema:** todas as consultas passam pelo Room e são
parametrizadas, portanto não há injeção de SQL; não existe `WebView` nem
`rawQuery`; nenhum `Log` escreve dados de utilizador; o manifesto não pede
permissões perigosas e apenas o *launcher* está exportado; `local.properties`
e ficheiros de chave não estão em controlo de versões.

---

## 3. Achados e correções

### 3.1 Severidade alta

#### A1 — Os dados saíam do dispositivo por backup automático

O manifesto declarava `allowBackup="true"` e os ficheiros de regras de backup
eram os templates vazios do Android Studio, apenas com comentários.

**Consequência:** o ficheiro `stockify.db` — contas de utilizador, hashes de
password e todo o inventário — e as SharedPreferences da sessão eram copiados
para a cloud da Google e podiam ser extraídos com `adb backup`.

**Correção:** `allowBackup="false"`, que cobre as versões anteriores ao
Android 12, mais um `data_extraction_rules.xml` com exclusão explícita em
`cloud-backup` **e** em `device-transfer`. A separação é necessária porque, a
partir do Android 12, a transferência dispositivo-a-dispositivo ignora o
`allowBackup` e tem de ser fechada em separado. Confirmado no manifesto
fundido que é empacotado no APK.

#### A2 — Passwords guardadas com SHA-256 simples, sem sal

Um único passo de digest, sem sal e sem iterações. Vulnerável a *rainbow
tables*, e duas contas com a mesma password ficavam com hashes idênticos,
revelando esse facto a quem lesse a base de dados. A comparação no login usava
`!=`, que termina no primeiro byte diferente.

**Correção:** `Seguranca.kt` reescrito com PBKDF2-HMAC-SHA1, 210 000 iterações
(recomendação OWASP para este algoritmo) e sal aleatório de 16 bytes por
conta, guardado no formato `iteracoes:sal:hash`. A comparação passou a usar
`MessageDigest.isEqual`, de tempo constante. Tudo vem do JDK — nenhuma
dependência foi acrescentada.

Não se usou SHA-256 na derivação porque `PBKDF2WithHmacSHA256` só existe a
partir da API 26 e o `minSdk` do projeto é 24. O código documenta como trocar
caso o `minSdk` venha a subir.

#### A3 — Movimentos de stock podiam perder-se e gerar stock negativo

O diálogo de movimentos lia o produto para memória, calculava a nova
quantidade e reescrevia o objeto inteiro. Duas saídas em simultâneo perdiam
uma, porque a segunda escrita se sobrepunha à primeira. Além disso, a
verificação `novaQuantidade < 0` estava desligada da escrita, deixando uma
janela para o stock ficar negativo.

**Correção:** o ajuste passou a ser uma única instrução SQL, com o limite
dentro da própria condição:

```sql
UPDATE produtos SET quantidade = quantidade + :delta
WHERE id = :id AND quantidade + :delta >= 0
```

O ajuste e o registo do movimento correm agora numa transação Room. Quando não
há stock suficiente, nada é gravado e o utilizador vê a mensagem de erro com a
quantidade real lida nesse momento, e não com a que estava no ecrã.

### 3.2 Severidade média

#### A4 — Coroutine sem ciclo de vida no diálogo de movimentos

Usava `CoroutineScope(Dispatchers.IO)`, desligado de qualquer ciclo de vida:
se o ecrã fosse destruído a meio da gravação, os callbacks tocavam num diálogo
e num contexto já destruídos.

**Correção:** o diálogo passou a receber o `lifecycleScope` do Fragment. Como
os DAOs `suspend` do Room já saltam da thread principal por si próprios, o
`Dispatchers.IO` e o `withContext(Main)` deixaram de ser necessários — o
ficheiro ficou 11 linhas mais curto.

#### A5 — Trabalho de disco na thread principal durante o arranque

`StockifyDatabase.obter()` forçava a abertura da base de dados com
`openHelper.writableDatabase` e semeava os dados iniciais de forma síncrona, a
partir do `onCreate` de uma Activity.

**Correção:** substituído por `RoomDatabase.Callback` com `onCreate` e
`onDestructiveMigration`. O segundo cobre exatamente o cenário que o
comentário original apontava como razão para não usar callbacks: a recriação
do schema sobre um ficheiro de base de dados já existente. A abertura passou a
ser preguiçosa e o preenchimento inicial corre na thread da base de dados.
Removeu 25 linhas de código manual.

#### A6 — Credenciais de demonstração em claro no código

`admin`/`password123`, `cesae`/`cesae` e `diana`/`diana123`, semeadas em todas
as builds, incluindo release.

**Decisão:** mantidas, por serem exigidas pelo enunciado do trabalho prático.
Estão agora concentradas num único ponto identificado, com comentário a nomear
o risco e o caminho de saída, e documentadas no README. Passam a ser guardadas
com PBKDF2 e sal, pelo que o ficheiro da base de dados deixa de ser, por si
só, uma lista de passwords.

#### A7 — Migração destrutiva da base de dados

`fallbackToDestructiveMigration` apaga os dados locais a cada mudança de
versão do schema.

**Decisão:** mantida, por ser consciente numa aplicação académica. Marcada no
código e listada no README como limitação conhecida.

### 3.3 Severidade baixa

**A8 — Rascunho do formulário sobrevivia ao encerramento de sessão.** O objeto
que guarda o formulário por gravar era global ao processo, pelo que reaparecia
ao utilizador seguinte. Passa a ser limpo ao terminar sessão.

**A9 — Validação de email fraca.** A verificação `contains("@") &&
contains(".")` aceitava valores como `a@`, `@b.pt` ou `a b@c.pt`. Substituída
por `Patterns.EMAIL_ADDRESS`, da própria plataforma Android.

**A10 — Mínimo de password inconsistente.** Seis caracteres no registo, cinco
na edição de perfil, o que permitia enfraquecer a própria conta depois de a
criar. Uniformizado em seis nos dois ecrãs.

**A11 — SKU sem restrição de unicidade.** Dois produtos podiam partilhar o
mesmo SKU em silêncio. Passou a existir índice único na base de dados, com
aviso no formulário antes de gravar e tratamento da exceção para o caso de
duas gravações simultâneas.

**A12 — Build de release sem R8.** Ver secção 5.

---

## 4. Testes

### 4.1 Situação inicial

Dois ficheiros-template: `assertEquals(4, 2 + 2)` e uma verificação ao nome do
package. Cobertura efetiva de zero. Nenhuma integração contínua configurada.

### 4.2 Preparação

Para tornar a lógica testável sem emulador, duas zonas de código passaram a
funções de topo puras, no mesmo ficheiro onde já viviam — sem classes novas,
sem interfaces e sem injeção de dependências:

- `StockViewModel.aplicarFiltros()` → `filtrarOrdenar(...)`
- as seis agregações do `InicioViewModel` → `somaStock`, `contarEmAlerta`,
  `somaReceita`, `somaLucro`, `receitaPorCategoria`, `top3PorValor`

### 4.3 Testes escritos

**`SegurancaTest` — 4 testes.** Que a mesma password gera hashes diferentes,
o que prova o funcionamento do sal; que a password correta valida; que a
errada, a vazia e a com maiúsculas trocadas não validam; e que um hash no
formato antigo ou corrompido devolve `false` em vez de fazer a aplicação
terminar.

**`FiltrosTest` — 8 testes.** Pesquisa por nome e por SKU, indiferente a
maiúsculas; filtro por categoria e por categoria inexistente; pesquisa e
filtro em simultâneo; as três ordenações disponíveis (nome, quantidade
crescente, valor em stock decrescente); comportamento com lista vazia.

**`EstatisticasTest` — 6 testes.** As seis contas do dashboard sobre uma lista
que inclui um produto esgotado e um produto vendido abaixo do preço de custo:
soma de stock, contagem de alertas, receita, lucro (incluindo o caso de lucro
negativo), receita por categoria — que tem de esconder as categorias a zero —
e o top 3. Todas verificadas também com lista vazia.

**`MovimentoDaoTest` — 4 testes instrumentados.** Sobre uma base de dados Room
em memória: saída válida desconta e regista; entrada acrescenta; saída maior
que o stock devolve `false` e não grava movimento nenhum. O quarto é o teste
central desta auditoria: dez saídas de uma unidade lançadas em paralelo sobre
um stock de cinco têm de dar exatamente cinco sucessos, quantidade final zero
e cinco movimentos gravados. Com o código anterior passavam quase todas e o
stock ficava negativo.

### 4.4 Resultados

| Suite | Testes | Falhas | Onde corre |
|---|---:|---:|---|
| SegurancaTest | 4 | 0 | JVM |
| FiltrosTest | 8 | 0 | JVM |
| EstatisticasTest | 6 | 0 | JVM |
| MovimentoDaoTest | 4 | 0 | Pixel 8 (emulador) |
| **Total** | **22** | **0** | |

Os dois ficheiros-template foram apagados.

### 4.5 Verificações executadas

| Comando | Resultado |
|---|---|
| `./gradlew testDebugUnitTest` | 18 testes, 0 falhas |
| `./gradlew connectedDebugAndroidTest` | 4 testes, 0 falhas, 2,2 s |
| `./gradlew assembleDebug` | sucesso |
| `./gradlew assembleRelease` | sucesso |
| `./gradlew lintDebug` | 0 erros, 80 avisos, todos pré-existentes |
| Manifesto fundido do APK | `allowBackup="false"` e regras de extração presentes |

Vale registar que foi o próprio lint a apanhar um erro na primeira versão da
correção A1: os ficheiros de regras de backup tinham sido apagados por serem
templates vazios, o que deixava a transferência dispositivo-a-dispositivo
aberta no Android 12 e posteriores. Corrigido antes de fechar a auditoria.

---

## 5. O que ficou por fazer, e porquê

**R8 desligado no build de release.** O APK de release sai sem encolhimento
nem ofuscação de código. No AGP 9.2, ligar `optimization.enable` exige a flag
experimental `android.r8.gradual.support`. Os problemas causados pelo R8
manifestam-se em execução e não na compilação, pelo que um release ofuscado
que não chegue a ser testado num dispositivo é pior do que um release legível.
A decisão está documentada no `build.gradle.kts` e no README, com a condição
que a deve reverter.

**Riscos aceites, documentados no README:**

- Base de dados sem encriptação. Encriptá-la exigiria SQLCipher, e o ficheiro
  só é legível com acesso físico ao dispositivo e privilégios de root.
- Sessão guardada em SharedPreferences, sem expiração.
- Sem limite de tentativas de login. A aplicação é local e não tem rede, pelo
  que um ataque por força bruta exigiria acesso físico continuado.

**Fora de âmbito:** servidor e API, autenticação biométrica, testes de
interface com Espresso e integração contínua.

---

## 6. Ficheiros alterados

17 ficheiros de código alterados, 4 ficheiros de teste criados e 3 apagados.
Cerca de 260 linhas acrescentadas e 170 removidas na aplicação.

| Zona | Ficheiros |
|---|---|
| Segurança | `util/Seguranca.kt`, `LoginActivity.kt`, `RegistoActivity.kt`, `ui/Perfil/EditarPerfilFragment.kt` |
| Dados | `data/local/StockifyDatabase.kt`, `data/local/ProdutoDao.kt`, `data/model/Produto.kt` |
| Stock | `ui/stocks/MovimentoDialogo.kt`, `ui/stocks/StockFragment.kt`, `ui/stocks/StockViewModel.kt`, `ui/stocks/ProdutoFormFragment.kt` |
| Dashboard | `ui/inicio/InicioViewModel.kt` |
| Perfil | `ui/Perfil/PerfilFragment.kt` |
| Configuração | `AndroidManifest.xml`, `res/xml/data_extraction_rules.xml`, `app/build.gradle.kts`, `README.md` |
| Testes criados | `SegurancaTest.kt`, `FiltrosTest.kt`, `EstatisticasTest.kt`, `MovimentoDaoTest.kt` |
| Apagados | `ExampleUnitTest.kt`, `ExampleInstrumentedTest.kt`, `res/xml/backup_rules.xml` |

A versão da base de dados subiu de 2 para 3. No primeiro arranque após esta
alteração a base é recriada e as contas de demonstração são semeadas já com
PBKDF2. Contas criadas por registo antes desta alteração perdem-se, o que está
avisado no README.

---

## 7. Verificação manual pendente

Em emulador, desinstalando a aplicação primeiro para forçar uma base de dados
nova:

- login com `admin` / `password123`, que confirma que o preenchimento inicial
  reescreveu os hashes em PBKDF2;
- password errada a apresentar erro no campo, sem entrar;
- registo de conta nova, encerramento de sessão e entrada com essa conta;
- alteração de password no perfil e entrada com a nova;
- saída superior ao stock a ser recusada sem fechar o diálogo;
- criação de produto com SKU já existente a apresentar erro.
