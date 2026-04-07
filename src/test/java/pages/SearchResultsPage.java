package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import io.qameta.allure.Step;

import java.util.regex.Pattern;

/** Página de resultados da busca no blog. */
public class SearchResultsPage {

    /** Mensagem do tema quando não acha post (WordPress). */
    public static final String NO_RESULTS_FULL_TEXT =
            "Lamentamos, mas nada foi encontrado para sua pesquisa, tente novamente com outras palavras.";

    private final Page page;

    private final Locator resultsHeading;
    private final Locator articleCards;
    private final Locator noResultsMessage;
    private final Locator newsletterSidebar;

    public SearchResultsPage(Page page) {
        this.page = page;
        this.resultsHeading = page.getByRole(
                AriaRole.HEADING,
                new Page.GetByRoleOptions().setName(Pattern.compile("Resultados encontrados para", Pattern.CASE_INSENSITIVE)));
        this.articleCards = page.locator("main article, article.post, article.type-post, .post");
        this.noResultsMessage = page.getByText(Pattern.compile("Lamentamos, mas nada foi encontrado", Pattern.CASE_INSENSITIVE));
        this.newsletterSidebar = page.locator(
                "aside#secondary, aside.widget-area, .sidebar-primary, aside .widget, [class*='newsletter' i], [class*='sidebar' i]")
                .first();
    }

    @Step("Ler heading de resultados")
    public Locator resultsHeading() {
        return resultsHeading;
    }

    @Step("Ler lista de cards de artigos")
    public Locator articleCards() {
        return articleCards;
    }

    @Step("Ler mensagem de nenhum resultado")
    public Locator noResultsMessage() {
        return noResultsMessage;
    }

    @Step("Ler barra de busca secundária")
    public Locator secondarySearchBar() {
        var inputs = page.locator("input[name='s']");
        int n = inputs.count();
        if (n > 1) {
            return inputs.nth(1);
        }
        return inputs.first();
    }

    @Step("Ler sidebar/newsletter")
    public Locator newsletterSidebar() {
        return newsletterSidebar;
    }
}
