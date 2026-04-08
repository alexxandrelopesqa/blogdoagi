package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Step;

/** Post único (single) no blog. */
public class SinglePostPage {

    private final Page page;
    private final String url;

    public SinglePostPage(Page page, String absoluteUrl) {
        this.page = page;
        this.url = absoluteUrl;
    }

    @Step("Abrir post")
    public SinglePostPage open() {
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

    public Locator article() {
        return page.locator("article, main article").first();
    }

    public Locator titleHeading() {
        return page.locator("h1.entry-title, article h1, main h1").first();
    }
}
