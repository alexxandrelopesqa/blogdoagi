package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Step;

/** Arquivo de categoria (WordPress). */
public class CategoryArchivePage {

    private final Page page;
    private final String url;

    public CategoryArchivePage(Page page, String absoluteUrl) {
        this.page = page;
        this.url = absoluteUrl;
    }

    @Step("Abrir arquivo de categoria")
    public CategoryArchivePage open() {
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
        return this;
    }

    public Locator mainLandmark() {
        return page.locator("main#primary, main, #primary").first();
    }

    /** Cards ou links de posts no arquivo. */
    public Locator postEntries() {
        return page.locator(
                "main article, main h2.entry-title a, .ast-archive-post, article.post, "
                        + "main .ast-row article, .site-content article, main ul li a[href*='/']");
    }
}
