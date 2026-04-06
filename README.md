# Blog do Agi - Automacao Web (Playwright + JUnit 5 + Allure)

[![Playwright E2E + Allure](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml/badge.svg)](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Playwright](https://img.shields.io/badge/Playwright-1.58.0-2ead33?logo=playwright)](https://playwright.dev/java/)

Suite E2E para o desafio de automacao do Agibank, focada no fluxo de busca do [Blog do Agi](https://blog.agibank.com.br/).

## Comece em 2 minutos

```bash
git clone https://github.com/alexxandrelopesqa/blogdoagi
cd blogdoagi
./mvnw -B exec:java
./mvnw clean test
./mvnw allure:report
```

Abra o relatorio em `target/site/allure-maven-plugin/index.html`.

## O que este projeto cobre

- Busca por termo existente (`Investimentos`)
- Busca por termo inexistente (`xyz123_nonexistent_search`)
- Validacao de URL, lista de resultados e estabilidade de layout
- Evidencias por teste no Allure (screenshot, video, trace e logs)

## Arquitetura

Estrutura principal:

```text
src/test/java/
├── core/BaseTest.java
├── pages/BlogHomePage.java
├── pages/SearchResultsPage.java
└── tests/BlogSearchTest.java
```

Decisoes adotadas:

- `Page Object Model` para separar interacao de UI e assercoes
- `PlaywrightAssertions.assertThat` para validacoes robustas
- Configuracao por ambiente (`BROWSER`, `HEADLESS`, `CI`)
- Pipeline multi-browser no GitHub Actions

## Pre-requisitos

| Ferramenta | Versao sugerida | Uso |
|---|---|---|
| Java | 17+ | Compilacao e execucao |
| Maven | nao obrigatorio (wrapper incluso) | Build, testes e relatorios |
| Git | recente | Clone e versionamento |
| Docker | opcional | Execucao isolada |

## Execucao local

### 1) Clonar repositorio

```bash
git clone https://github.com/alexxandrelopesqa/blogdoagi
cd blogdoagi
```

### 2) Instalar dependencias do projeto

```bash
./mvnw -B clean install -DskipTests
```

### 3) Instalar navegadores Playwright (uma vez)

```bash
./mvnw -B exec:java
```

### 4) Rodar suite completa

```bash
./mvnw clean test
```

### 5) Rodar por navegador

Windows PowerShell:

```powershell
$env:BROWSER = "chromium"; .\mvnw.cmd clean test
$env:BROWSER = "firefox";  .\mvnw.cmd clean test
$env:BROWSER = "webkit";   .\mvnw.cmd clean test
```

Linux/macOS:

```bash
BROWSER=chromium ./mvnw clean test
BROWSER=firefox  ./mvnw clean test
BROWSER=webkit   ./mvnw clean test
```

### 6) Rodar em modo visual (headed)

Windows PowerShell:

```powershell
$env:HEADLESS = "false"; .\mvnw.cmd clean test
```

Linux/macOS:

```bash
HEADLESS=false ./mvnw clean test
```

## Variaveis de ambiente

| Variavel | Default | Valores esperados | Efeito |
|---|---|---|---|
| `BROWSER` | `chromium` | `chromium`, `firefox`, `webkit` | Define engine de execucao |
| `HEADLESS` | `true` no CI, caso contrario `false` | `true`/`false` | Executa com/sem UI |
| `CI` | ausente local | `true`/ausente | Ajusta comportamento para pipeline |
| `BASE_URL` | `https://blog.agibank.com.br` | URL valida (http/https) | Permite apontar para ambiente local/staging |
| `PLAYWRIGHT_BROWSERS_PATH` | cache global do usuario | caminho local | Mantem browsers Playwright dentro do projeto |
| `ATTACH_EVIDENCE` | `true` local / `true` CI | `true`/`false` | Controla anexos sensiveis (screenshot/video/trace/logs) |

## Relatorio Allure

### Gerar relatorio local

```bash
./mvnw allure:report
```

Saida principal:

- HTML: `target/site/allure-maven-plugin`
- Resultados brutos: `target/allure-results`
- Artefatos extras: `target/artifacts`

### Evidencias geradas por teste

- Screenshot (`.png`)
- Video (`.webm`)
- Trace (`.zip`)
- Logs de runtime (`.txt`)
- Metadata (`environment.properties`, `executor.json`, `categories.json`)

### Dica importante

Se abrir o Allure por `file://` e ficar em "Loading...", sirva por HTTP local:

```bash
cd target/site/allure-maven-plugin
py -m http.server 5050
```

Depois abra `http://localhost:5050`.

## Docker

Build:

```bash
docker build -t blog-do-agi-tests .
```

Execucao:

```bash
docker run --rm --ipc=host blog-do-agi-tests
```

O container executa com usuario nao-root (`pwuser`) e usa `mvnw` interno para reduzir acoplamento com ferramenta global.

## CI/CD (GitHub Actions)

Workflow: `.github/workflows/playwright.yml`

O pipeline executa:

1. Checkout
2. Java 17 + cache Maven
3. Instalacao de browsers (`./mvnw -B -q exec:java`)
4. Testes em matriz (`chromium`, `firefox`, `webkit`)
5. Upload dos resultados brutos (`allure-results-<browser>`)
6. Job agregador baixa todos os `allure-results-*`, reaproveita `history` da ultima publicacao em `gh-pages` e gera relatorio consolidado
7. Publica no GitHub Pages:
   - `latest` na raiz
   - snapshot por execucao em `runs/<run_number>`

Seguranca aplicada no workflow:

- Actions fixadas por commit SHA (supply-chain hardening)
- Publicacao em PR apenas quando o PR pertence ao mesmo repositório (evita falha de permissao em forks)

### Evidencias no GitHub Actions

As evidencias ficam habilitadas no CI por padrao (`ATTACH_EVIDENCE=true`) para manter rastreabilidade completa no Allure:

- screenshot
- video
- trace
- logs de runtime

## Historico e rastreabilidade no Allure (GitHub Pages)

O projeto preserva o historico entre execucoes copiando a pasta `history` da publicacao anterior antes de gerar o novo report.

Com isso, no Allure Pages voce tem:

- **Tendencias historicas** (widgets de trend)
- **Ultima execucao** na raiz do Pages
- **Snapshot por execucao** em `runs/<run_number>`

## GitHub Pages

No repositorio, configure:

1. `Settings > Pages`
2. Source: `Deploy from a branch`
3. Branch: `gh-pages` e pasta `/(root)`

A publicacao ocorre apos pipeline verde em push para `main`/`master`.

## Troubleshooting rapido

### `Unknown lifecycle phase ".classpathScope=test"` no PowerShell

Use:

```powershell
.\mvnw.cmd -B exec:java
```

## Execucao local com baixa dependencia externa

Para reduzir dependencia de ferramentas instaladas na maquina e permitir execucoes offline apos o bootstrap:

1. Use sempre `mvnw`/`mvnw.cmd` (Maven Wrapper incluso no repo).
2. Configure cache local dos browsers Playwright no proprio projeto:

```powershell
$env:PLAYWRIGHT_BROWSERS_PATH = ".playwright-browsers"
.\mvnw.cmd -B exec:java
.\mvnw.cmd -B clean test
```

3. Depois do primeiro bootstrap online, rode em modo offline:

```powershell
$env:PLAYWRIGHT_BROWSERS_PATH = ".playwright-browsers"
.\mvnw.cmd -o clean test
```

Observacao: para testar o site real (`blog.agibank.com.br`) ainda e necessario acesso a internet. Se quiser execucao 100% offline, aponte `BASE_URL` para um ambiente local espelho/mocado.

### Allure abre sem dados (só "Loading...")

Abra por HTTP local (nao por `file://`) conforme secao de Allure.

### Erro de push com 403 no GitHub

Token sem escopo de escrita em repo/workflow. Gere token com:

- `repo` (classic) ou `Contents: Read and write` (fine-grained)
- `workflow` (classic) ou permissao equivalente para alterar `.github/workflows/*`

## Licenca

Uso educacional / desafio tecnico. Marca Agibank e conteudo do blog pertencem aos respectivos titulares.
