package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Step;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static core.BaseTest.BASE_URL;

/**
 * Página inicial do blog — busca (lupa / campo / envio) e botão flutuante (chat).
 */
public class BlogHomePage {

    private final Page page;

    /** Campo de busca WordPress padrão ({@code s}). */
    private final Locator searchInput;
    /** Ícone “lupa” ou alternador de busca em temas responsivos. */
    private final Locator searchToggle;
    /** Botão de envio dentro do formulário de busca. */
    private final Locator searchSubmit;
    /** Widget de chat / atendimento fixo (Social Chat e similares). */
    private final Locator floatingChatWidget;

    public BlogHomePage(Page page) {
        this.page = page;
        this.searchInput = page.locator("input[name='s']").first();
        this.searchToggle = page.locator(
                "button[aria-label*='Busca' i], button[aria-label*='Search' i], "
                        + ".search-toggle, .ast-search-menu-icon, a[href*='#search'], [class*='search-icon']")
                .first();
        this.searchSubmit = page.locator("form[role='search'] button[type='submit'], .search-submit, button.search-submit")
                .first();
        this.floatingChatWidget = page.locator(
                "[class*='social-chat' i], [id*='chat' i], [class*='floating-chat' i], "
                        + "iframe[title*='chat' i], [data-widget*='chat' i]")
                .first();
    }

    @Step("Acessar a home do Blog do Agi")
    public BlogHomePage navigate() {
        page.navigate(BASE_URL);
        page.waitForLoadState();
        return this;
    }

    /**
     * Garante que o campo de busca esteja utilizável (abre overlay se necessário).
     */
    @Step("Garantir que a busca esteja visível")
    public BlogHomePage ensureSearchVisible() {
        if (visibleSearchInput().count() == 0) {
            if (searchToggle.count() > 0) {
                searchToggle.click();
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
            Locator submit = visibleSearchSubmit();
            if (submit.count() > 0) {
                submit.click();
            } else {
                input.press("Enter");
            }
            page.waitForURL(url -> url.contains("?s=") || url.contains("&s="));
        } else {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String searchUrl = BASE_URL + "/?s=" + encoded;
            try {
                page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            } catch (PlaywrightException firstFailure) {
                // Retry curto para falhas transitórias de navegação (net::ERR_ABORTED).
                page.waitForTimeout(500);
                page.navigate(searchUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            }
        }
        return new SearchResultsPage(page);
    }

    public Locator floatingActionOrChat() {
        return floatingChatWidget;
    }

    private Locator visibleSearchInput() {
        return page.locator("input[name='s']:visible").first();
    }

    private Locator visibleSearchSubmit() {
        return page.locator(
                        "form[role='search'] button[type='submit']:visible, .search-submit:visible, button.search-submit:visible")
                .first();
    }
}
