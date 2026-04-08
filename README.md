# Blog do Agi — testes E2E (Playwright + JUnit 5 + Allure)

[![CI](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml/badge.svg)](https://github.com/alexxandrelopesqa/blogdoagi/actions/workflows/playwright.yml)

Testes de ponta a ponta no [Blog do Agi](https://blog.agibank.com.br/): busca com resultado e busca sem resultado. Stack em Java 17, relatório no Allure e pipeline no GitHub Actions (com opção de rodar no Jenkins).

## Rodar na sua máquina

```bash
git clone https://github.com/alexxandrelopesqa/blogdoagi.git
cd blogdoagi
./mvnw -B exec:java    # browsers do Playwright (só na primeira vez ou após atualizar versão)
./mvnw clean test
./mvnw allure:report
```

**Grupos JUnit:** `./mvnw -Psmoke test` (rápido: home + busca) ou `./mvnw -Pregression test` (tudo com tag `regression`, incluindo responsividade e templates). URLs canônicas de regressão ficam em `src/test/resources/regression.properties`.

Relatório HTML: `target/site/allure-maven-plugin/index.html`.  
Se o navegador ficar só em “Loading…”, suba um servidor HTTP na pasta do relatório (ex.: `py -m http.server 5050`) e abra `http://localhost:5050`.

No Windows PowerShell use `.\mvnw.cmd` no lugar de `./mvnw`.

## O que os testes fazem

1. **Smoke** — home com título e `main` visível (`BlogSmokeTest`).  
2. **Busca** — resultado com **Investimentos** e cenário sem resultado (`BlogSearchTest`).  
3. **Responsividade** — vários viewports + busca (`BlogResponsiveLayoutTest`).  
4. **Regressão** — categoria, posts canônicos, `/page/2/`, Web Stories, HTTP 404 em slug inventado (`BlogRegressionTest`).

Page Objects em `pages/`, URLs de regressão via `core/RegressionPaths.java`, setup em `core/BaseTest.java`.

## Premissas e manutenção

- **Alvo:** Os testes são E2E no URL de `BASE_URL` (por padrão o blog em produção). Se o site estiver em baixo, lento ou em manutenção, a suíte pode falhar sem indicação de bug no código de teste.
- **Dados canónicos:** Caminhos em `src/test/resources/regression.properties` (categoria, artigos como `/cdb/` e `/cdi/`, slug de 404) têm de ser revistos quando o conteúdo ou os permalinks do WordPress mudarem.
- **Escopo:** Foco em busca, páginas de arquivo/post/Web Stories e alguns viewports. Não cobre API backend, acessibilidade sistemática (axe), performance (Core Web Vitals) nem dispositivos reais — isso seria camada extra.
- **CI:** O workflow corre **Chromium, Firefox e WebKit** em paralelo (três jobs); o tempo total depende do GitHub Actions e da rede até ao blog.

## Variáveis de ambiente (úteis)

| Variável | O que faz |
|----------|-----------|
| `BROWSER` | `chromium` (padrão), `firefox` ou `webkit` |
| `HEADLESS` | `true`/`false`; se não definir, headless quando `CI` existir |
| `BASE_URL` | URL do blog (padrão: produção) |
| `PLAYWRIGHT_BROWSERS_PATH` | Onde guardar os browsers (ex.: `.playwright-browsers` no projeto) |
| `ATTACH_EVIDENCE` | `true`/`false` — screenshot, vídeo, trace e logs no Allure |

## Docker

```bash
docker build -t blog-do-agi-tests .
docker run --rm --ipc=host blog-do-agi-tests
```

A imagem usa a base oficial Playwright Java (mesma linha de versão do `pom.xml`) e roda os testes como usuário `pwuser`.

## GitHub Actions e Pages

O workflow está em `.github/workflows/playwright.yml`: roda os três browsers, sobe os `allure-results` por browser, junta tudo num job que gera o relatório e publica no **GitHub Pages** (branch `gh-pages`).  
O relatório mais recente fica na raiz do site; cada execução também fica em `runs/<número_do_run>/`.  
O Allure reaproveita a pasta `history` da publicação anterior para manter gráficos de tendência.

**Pages:** Settings → Pages → branch `gh-pages`, pasta `/ (root)`.

Em PR de fork de outro repositório o deploy costuma não rodar por limite de permissão do token — é esperado.

## Jenkins

Tem `Jenkinsfile` na raiz: parâmetros `BROWSER`, `ATTACH_EVIDENCE` e `BASE_URL`. No fim do job ficam arquivados `target/allure-results`, o HTML do Allure e `target/artifacts`.

## Problemas comuns

- **PowerShell e Maven:** use `.\mvnw.cmd -B exec:java` (sem colar propriedades `-D` soltas que o shell interpreta errado).  
- **Push 403:** token precisa de permissão de escrita no repo e, se mudar workflow, permissão de workflow.  
- **Site fora ou lento:** os testes batem na URL real; falha de rede ou mudança de layout pode quebrar cenário até ajustar locators.

## Licença

Uso em contexto de desafio/portfólio. Marca Agibank e conteúdo do blog são dos respectivos titulares.
