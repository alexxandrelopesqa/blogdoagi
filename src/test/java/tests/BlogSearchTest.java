package tests;

import core.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.TmsLink;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.BlogHomePage;
import pages.SearchResultsPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Busca no Blog")
@Feature("Busca no Blog do Agi")
class BlogSearchTest extends BaseTest {

    @Test
    @Story("Busca por termo existente")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Busca com retorno: URL, heading e primeiro card.")
    @TmsLink("AGI-SEARCH-001")
    @DisplayName("Busca Investimentos — resultados e link no card")
    void buscaInvestimentos_retornaResultados() {
        try {
            var results = new BlogHomePage(page).navigate().search("Investimentos");

            io.qameta.allure.Allure.step("URL com parâmetro de busca");
            assertThat(page).hasURL(Pattern.compile(".*[?&]s=Investimentos.*"));
            io.qameta.allure.Allure.step("Heading e cards visíveis");
            assertThat(results.resultsHeading()).isVisible();
            assertThat(results.articleCards().first()).isVisible();
            assertThat(results.articleCards().first().locator("a").first()).isVisible();
        } finally {
            finalizeEvidenceInTestContext("cenario1-investimentos");
        }
    }

    @Test
    @Story("Busca sem resultados")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.NORMAL)
    @Description("Busca vazia: mensagem padrão e layout (sidebar ou chat).")
    @TmsLink("AGI-SEARCH-002")
    @DisplayName("Busca sem resultado — mensagem e layout")
    void buscaSemResultados_preservaLayout() {
        try {
            var home = new BlogHomePage(page).navigate();
            var results = home.search("xyz123_nonexistent_search");

            io.qameta.allure.Allure.step("URL e texto de 'nada encontrado'");
            assertThat(page).hasURL(Pattern.compile(".*[?&]s=xyz123_nonexistent_search.*"));
            assertThat(page.getByText(SearchResultsPage.NO_RESULTS_FULL_TEXT)).isVisible();
            io.qameta.allure.Allure.step("Barra de busca e sidebar ou chat");
            assertThat(results.secondarySearchBar()).isVisible();
            assertThat(results.newsletterSidebar().or(home.floatingActionOrChat())).isVisible();
        } finally {
            finalizeEvidenceInTestContext("cenario2-sem-resultado");
        }
    }
}
