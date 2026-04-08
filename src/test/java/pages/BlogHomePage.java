package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Step;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import static core.BaseTest.BASE_URL;

/** Home do blog: busca e (se existir) botão flutuante de chat. */
public class BlogHomePage {

    /** Evita considerar ?s= vazio como sucesso (WordPress pode redirecionar assim após Enter). */
    private static final Pattern HAS_NONEMPTY_SEARCH_PARAM = Pattern.compile(".*[?&]s=[^&].*");

    private final Page page;

    private final Locator searchInput;
    private final Locator searchToggle;
    private final Locator floatingChatWidget;

    public BlogHomePage(Page page) {
        this.page = page;
        this.searchInput = page.locator("input[name='s']").first();
        this.searchToggle = page.locator(
                "button[aria-label*='Busca' i], button[aria-label*='Search' i], "
                        + ".search-toggle, .ast-search-menu-icon, a[href*='#search'], [class*='search-icon']")
                .first();
        this.floatingChatWidget = page.locator(
                "[class*='social-chat' i], [id*='chat' i], [class*='floating-chat' i], "
                        + "iframe[title*='chat' i], [data-widget*='chat' i]")
                .first();
    }

    @Step("Acessar a home do Blog do Agi")
    public BlogHomePage navigate() {
        Page.NavigateOptions opts = new Page.NavigateOptions()
                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
                .setTimeout(60_000);
        try {
            page.navigate(BASE_URL, opts);
        } catch (PlaywrightException first) {
            page.waitForTimeout(800);
            page.navigate(BASE_URL, opts);
        }
        page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(25_000));
        return this;
    }

    @Step("Garantir que a busca esteja visível")
    public BlogHomePage ensureSearchVisible() {
        if (visibleSearchInput().count() > 0) {
            return this;
        }
        if (searchToggle.count() == 0) {
            return this;
        }
        try {
            searchToggle.scrollIntoViewIfNeeded();
            searchToggle.click();
        } catch (PlaywrightException ignored) {
            try {
                searchToggle.click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true));
            } catch (PlaywrightException ignoredAgain) {
                // search() cai na navegação direta ?s= se não houver input visível
            }
        }
        return this;
    }

    @Step("Buscar por termo: {query}")
    public SearchResultsPage search(String query) {
        ensureSearchVisible();
        Locator input = visibleSearchInput();
        if (input.count() > 0) {
            input.fill(query);
            // Enter evita clique no submit coberto pelo menu mega / above-header (Astra no CI).
            input.press("Enter");
            try {
                page.waitForURL(
                        u -> HAS_NONEMPTY_SEARCH_PARAM.matcher(u).matches(),
                        new Page.WaitForURLOptions().setTimeout(25_000));
            } catch (PlaywrightException e) {
                navigateToSearchUrl(query);
            }
        } else {
            navigateToSearchUrl(query);
        }
        return new SearchResultsPage(page);
    }

    private void navigateToSearchUrl(String query) {
        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String searchUrl = BASE_URL + "/?s=" + encoded;
        try {
            page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        } catch (PlaywrightException firstFailure) {
            page.waitForTimeout(500);
            page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        }
    }

    public Locator floatingActionOrChat() {
        return floatingChatWidget;
    }

    private Locator visibleSearchInput() {
        return page.locator("input[name='s']:visible").first();
    }
}
