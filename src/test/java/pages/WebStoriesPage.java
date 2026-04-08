package pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import io.qameta.allure.Step;

/** Índice e single de Web Stories. */
public class WebStoriesPage {

    private final Page page;

    public WebStoriesPage(Page page) {
        this.page = page;
    }

    @Step("Abrir índice de Web Stories")
    public WebStoriesPage openIndex(String absoluteUrl) {
        navigateRelaxed(absoluteUrl);
        return this;
    }

    @Step("Abrir Web Story")
    public WebStoriesPage openStory(String absoluteUrl) {
        navigateRelaxed(absoluteUrl);
        return this;
    }

    private void navigateRelaxed(String absoluteUrl) {
        Page.NavigateOptions opts = new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED);
        try {
            page.navigate(absoluteUrl, opts);
        } catch (PlaywrightException first) {
            page.waitForTimeout(500);
            page.navigate(absoluteUrl, opts);
        }
        page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(25_000));
    }

    /** Links para slugs dentro de /web-stories/ (exclui o próprio índice). */
    public Locator storyLinks() {
        return page.locator("a[href*='/web-stories/'][href$='/']:not([href='/web-stories/'])");
    }

    public Locator mainContent() {
        return page.locator("main, #main, .web-stories-list, article").first();
    }
}
