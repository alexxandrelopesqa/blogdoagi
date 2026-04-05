# Blog do Agi — Automação Web (Playwright + JUnit 5 + Allure)

[![Playwright E2E + Allure](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml/badge.svg)](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml)
[![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Playwright](https://img.shields.io/badge/Playwright-1.58.0-2ead33?logo=playwright)](https://playwright.dev/java/)

## Sobre o projeto

Suite de testes end-to-end do **[Blog do Agi](https://blog.agibank.com.br/)**, no contexto do desafio de automação do Agibank. O foco é validar a **busca no blog** (fluxo feliz e cenário sem resultados), com **Page Object Model (POM)** explícito, separação de responsabilidades (`core`, `pages`, `tests`) e **Clean Code** (nomes claros, locators resilientes, sem lógica de negócio nas páginas).

**Decisões de arquitetura**

- **POM estrito**: `BlogHomePage` e `SearchResultsPage` encapsulam elementos e ações; os testes apenas orquestram e asserem.
- **Playwright Assertions** (`assertThat`): asserções auto-retry alinhadas ao modelo de UI do Playwright.
- **JUnit 5 + Allure**: relatórios ricos para CI e histórico; extensão Allure com autodetecção habilitada.
- **Configuração por ambiente**: modo headless via variável `HEADLESS` (ou `CI` por padrão).

## Pré-requisitos

| Ferramenta | Uso |
|------------|-----|
| **JDK 17** | Compilação e execução |
| **Maven 3.9+** | Build e dependências |
| **Git** | Clone do repositório |
| **Docker** (opcional) | Execução em contêiner com imagem oficial Playwright Java |

## Execução local

### 1. Clonar o repositório

```bash
git clone https://github.com/alexxandrelopesqa/blogdoagi
cd blogdoagi
```

### 2. Baixar dependências Maven

```bash
mvn -B clean install -DskipTests
```

### 3. Instalar os navegadores do Playwright

Os binários não vêm no JAR; instale uma vez (Linux/macOS/Windows):

```bash
mvn -B exec:java
```

No PowerShell, se preferir manter argumentos `-D` no futuro, use aspas para evitar parsing do shell:

```powershell
mvn -B "exec:java" "-Dalguma.propriedade=valor"
```

### 4. Rodar os testes

```bash
mvn clean test
```

Selecionando navegador por ambiente (`BROWSER`):

```powershell
# Windows PowerShell
$env:BROWSER = "chromium"; mvn clean test
$env:BROWSER = "firefox";  mvn clean test
$env:BROWSER = "webkit";   mvn clean test
```

```bash
# Linux / macOS
BROWSER=chromium mvn clean test
BROWSER=firefox  mvn clean test
BROWSER=webkit   mvn clean test
```

Para ver o navegador localmente (headless desligado por padrão quando `CI` não está definido):

```powershell
# Windows PowerShell
$env:HEADLESS = "false"; mvn clean test
```

```bash
# Linux / macOS
HEADLESS=false mvn clean test
```

## Relatório Allure (local)

Após `mvn test`, os resultados brutos ficam em `target/allure-results`.
Evidências adicionais (anexadas automaticamente no Allure) ficam em `target/artifacts`:

- screenshots (`target/artifacts/...` + anexo no Allure)
- vídeos por teste (`target/artifacts/videos`)
- traces Playwright (`target/artifacts/traces`)
- logs de runtime (console, page errors, request failures)
- metadata do relatório (`environment.properties`, `executor.json`, `categories.json`)

### Opção A — Maven (plugin Allure)

```bash
mvn allure:report
```

Abra o relatório gerado (geralmente em `target/site/allure-maven-plugin/index.html` no navegador).

### Opção B — Allure CLI

Instale o [Allure Commandline](https://github.com/allure-framework/allure2/releases) e execute:

```bash
allure serve target/allure-results
```

O navegador abrirá o relatório interativo.

## Docker

A imagem **deve** usar a mesma versão do Playwright do `pom.xml` (ver [documentação Playwright Java — Docker](https://playwright.dev/java/docs/docker)).

```bash
docker build -t blog-do-agi-tests .
docker run --rm --ipc=host blog-do-agi-tests
```

Recomendações da Microsoft: `--ipc=host` para Chromium em Docker; em produção, use `root` apenas para testes confiáveis.

## CI/CD (GitHub Actions)

O workflow `.github/workflows/playwright.yml`:

1. Checkout do código  
2. JDK 17 (Temurin) + cache Maven  
3. Instalação dos navegadores Playwright (`exec:java`)  
4. `mvn clean test` com `CI=true`, `HEADLESS=true` e matriz de `BROWSER` (`chromium`, `firefox`, `webkit`)  
5. Geração do relatório Allure com `mvn allure:report`  
6. Upload dos artefatos `allure-report-chromium`, `allure-report-firefox` e `allure-report-webkit`  
7. Upload dos resultados brutos `allure-results-chromium`, `allure-results-firefox` e `allure-results-webkit`  
8. Publicação no **GitHub Pages** (branch `gh-pages`) via job dedicado de deploy, usando o artefato `chromium`, apenas em pushes às branches `main` ou `master`

**Configuração no GitHub**

1. Em **Settings → Pages**, escolha a fonte **Deploy from a branch** e a branch `gh-pages` (raiz `/`).  
2. Se você fez fork ou renomeou o repositório, atualize as URLs nos **badges** (topo deste README) e o exemplo de `git clone` na seção **Execução local** para o seu `usuario/repositorio`.  
3. Após o primeiro push em `main` ou `master` com o workflow concluído, o relatório pode levar alguns minutos para aparecer no endereço do GitHub Pages.

## Estrutura do repositório

```
src/test/java/
├── core/BaseTest.java          # Ciclo de vida Playwright + headless
├── pages/BlogHomePage.java     # Busca (lupa, input, envio, chat flutuante)
├── pages/SearchResultsPage.java# Resultados, vazio, busca secundária, sidebar
└── tests/BlogSearchTest.java   # Cenários + assertThat
```

## Licença

Uso educacional / desafio técnico. Marca **Agibank** e conteúdo do blog pertencem aos respectivos titulares.
