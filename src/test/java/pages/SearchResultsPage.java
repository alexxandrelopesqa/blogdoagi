package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

/**
 * Resultados de busca — títulos, cards, mensagem vazia, busca secundária e sidebar.
 */
public class SearchResultsPage {

    /** Texto exibido pelo tema quando não há posts (cópia atual do WordPress). */
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

    public Locator resultsHeading() {
        return resultsHeading;
    }

    public Locator articleCards() {
        return articleCards;
    }

    public Locator noResultsMessage() {
        return noResultsMessage;
    }

    /**
     * Barra de busca “secundária”: segundo campo {@code s} quando existir; caso contrário o único visível.
     */
    public Locator secondarySearchBar() {
        var inputs = page.locator("input[name='s']");
        int n = inputs.count();
        if (n > 1) {
            return inputs.nth(1);
        }
        return inputs.first();
    }

    public Locator newsletterSidebar() {
        return newsletterSidebar;
    }
}
