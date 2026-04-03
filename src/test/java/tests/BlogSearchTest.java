package tests;

import core.BaseTest;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.BlogHomePage;
import pages.SearchResultsPage;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Feature("Busca no Blog do Agi")
class BlogSearchTest extends BaseTest {

    @Test
    @Story("Busca por termo existente")
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Cenário 1: busca 'Investimentos' — URL, título de resultados e card de artigo")
    void buscaInvestimentos_retornaResultados() {
        var results = new BlogHomePage(page).navigate().search("Investimentos");

        assertThat(page).hasURL(Pattern.compile(".*[?&]s=Investimentos.*"));
        assertThat(results.resultsHeading()).isVisible();
        assertThat(results.articleCards().first()).isVisible();
        assertThat(results.articleCards().first().locator("a").first()).isVisible();
    }

    @Test
    @Story("Busca sem resultados")
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Cenário 2: termo inexistente — mensagem, busca secundária e layout (sidebar + chat)")
    void buscaSemResultados_preservaLayout() {
        var home = new BlogHomePage(page).navigate();
        var results = home.search("xyz123_nonexistent_search");

        assertThat(page).hasURL(Pattern.compile(".*[?&]s=xyz123_nonexistent_search.*"));
        assertThat(page.getByText(SearchResultsPage.NO_RESULTS_FULL_TEXT)).isVisible();
        assertThat(results.secondarySearchBar()).isVisible();
        assertThat(results.newsletterSidebar().or(home.floatingActionOrChat())).isVisible();
    }
}
