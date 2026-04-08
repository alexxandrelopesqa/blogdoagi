package tests;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import core.BaseTest;
import core.RegressionPaths;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.CategoryArchivePage;
import pages.SinglePostPage;
import pages.WebStoriesPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Regressão")
@Feature("Templates e navegação")
@Tag("regression")
class BlogRegressionTest extends BaseTest {

    @Test
    @Story("Arquivo de categoria")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Categoria canônica carrega com main e entradas de conteúdo.")
    @DisplayName("REG-ARC-01 — arquivo de categoria")
    void categoriaEmprestimo_carregaArquivo() {
        try {
            var archive = new CategoryArchivePage(page, RegressionPaths.absoluteUrl("regression.path.category"));
            archive.open();
            assertThat(archive.mainLandmark()).isVisible();
            int entries = archive.postEntries().count();
            int inContentLinks =
                    page.locator("main a[href*='blog.agibank.com.br/'], main a[href^='/']").count();
            Assertions.assertTrue(
                    entries >= 1 || inContentLinks >= 8,
                    "Esperado posts ou links de conteúdo no arquivo (entries=" + entries + ", links=" + inContentLinks + ")");
        } finally {
            finalizeEvidenceInTestContext("reg-arc-categoria");
        }
    }

    @Test
    @Story("Post único")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Slugs canônicos de post abrem article e título.")
    @DisplayName("REG-POST-01 — posts evergreen (CDB e CDI)")
    void postsCanonicos_articleEh1Visiveis() {
        try {
            for (String key : new String[] {"regression.path.post.cdb", "regression.path.post.second"}) {
                var post = new SinglePostPage(page, RegressionPaths.absoluteUrl(key));
                post.open();
                assertThat(post.article()).isVisible();
                assertThat(post.titleHeading()).isVisible();
            }
        } finally {
            finalizeEvidenceInTestContext("reg-post-canonicos");
        }
    }

    @Test
    @Story("Paginação")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.NORMAL)
    @Description("Segunda página do arquivo principal responde e exibe conteúdo.")
    @DisplayName("REG-PAG-01 — /page/2/")
    void homePagina2_carrega() {
        try {
            String url = RegressionPaths.absoluteUrl("regression.path.archive.page2");
            Page.NavigateOptions opts = new Page.NavigateOptions()
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                    .setTimeout(60_000);
            try {
                page.navigate(url, opts);
            } catch (PlaywrightException e) {
                page.waitForTimeout(600);
                page.navigate(url, opts);
            }
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(25_000));
            assertThat(page).hasURL(Pattern.compile(".*/page/2/?($|\\?)"));
            assertThat(page.locator("main#primary, main, #primary, body").first()).isVisible();
            int blocks = page.locator(
                            "article, .ast-archive-post, main h2, .entry-title, .site-main .post, .ast-row .post")
                    .count();
            String text = page.locator("body").innerText();
            Assertions.assertTrue(
                    blocks >= 1 || text.length() > 2_500,
                    "Página 2 deve ter listagem ou corpo substancial (blocks=" + blocks + ", chars=" + text.length() + ")");
        } finally {
            finalizeEvidenceInTestContext("reg-pag-page2");
        }
    }

    @Test
    @Story("Web Stories")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.NORMAL)
    @Description("Índice lista stories; single abre com área principal.")
    @DisplayName("REG-WS-01/02 — índice e single Web Story")
    void webStories_indiceESingle() {
        try {
            var stories = new WebStoriesPage(page);
            stories.openIndex(RegressionPaths.absoluteUrl("regression.path.webstories.index"));
            assertThat(stories.mainContent()).isVisible();
            Locator storyAnchors = page.locator("a[href*='/web-stories/']");
            int links = storyAnchors.count();
            Assertions.assertTrue(links >= 4, "Esperado vários links para /web-stories/ no índice, encontrado: " + links);

            String href = storyAnchors.nth(Math.min(2, links - 1)).getAttribute("href");
            Assertions.assertNotNull(href, "href da Web Story");
            String storyUrl = resolveAbsoluteUrl(href);
            stories.openStory(storyUrl);
            assertThat(page).hasURL(Pattern.compile(".*/web-stories/.+"));
            Assertions.assertFalse(page.title().isBlank(), "Web Story deve ter título");
            assertThat(page.locator("body")).isVisible();
            Assertions.assertTrue(
                    page.locator("amp-story, [class*='web-story' i], main, article, #web-stories-wrapper")
                            .count()
                            >= 1,
                    "Estrutura de Web Story ou fallback de conteúdo não encontrada");
        } finally {
            finalizeEvidenceInTestContext("reg-ws-indice-single");
        }
    }

    @Test
    @Story("Erro 404")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.MINOR)
    @Description("Slug inexistente retorna HTTP 404.")
    @DisplayName("REG-NEG-02 — página inexistente")
    void slugInexistente_retorna404() {
        try {
            String url = RegressionPaths.absoluteUrl("regression.path.notfound");
            APIResponse response = page.context().request().get(url);
            Assertions.assertEquals(
                    404,
                    response.status(),
                    "GET " + url + " deveria retornar 404, veio: " + response.status());
        } finally {
            finalizeEvidenceInTestContext("reg-neg-404");
        }
    }

    private static String resolveAbsoluteUrl(String href) {
        if (href.startsWith("http://") || href.startsWith("https://")) {
            return href;
        }
        String base = BaseTest.BASE_URL.replaceAll("/$", "");
        return base + (href.startsWith("/") ? href : "/" + href);
    }
}
