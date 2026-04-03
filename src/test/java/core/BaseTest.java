package core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.Optional;

/**
 * Ciclo de vida do Playwright: um browser por suíte, contexto isolado por teste.
 * <p>
 * Headless: variável {@code HEADLESS} ({@code true}/{@code false}). Se ausente,
 * usa headless quando {@code CI} estiver definido (por exemplo, GitHub Actions).
 */
public abstract class BaseTest {

    public static final String BASE_URL = "https://blog.agibank.com.br";

    private static Playwright playwright;
    private static Browser browser;

    protected com.microsoft.playwright.Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        String browserName = browserName();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(isHeadless());
        browser = switch (browserName) {
            case "firefox" -> playwright.firefox().launch(launchOptions);
            case "webkit" -> playwright.webkit().launch(launchOptions);
            case "chromium" -> playwright.chromium().launch(launchOptions);
            default -> throw new IllegalArgumentException(
                    "Valor inválido para BROWSER: " + browserName + ". Use: chromium, firefox ou webkit.");
        };
        System.out.println("Running tests on browser: " + browserName);
    }

    @BeforeEach
    void createPage() {
        var context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closePage() {
        if (page != null) {
            page.context().close();
            page = null;
        }
    }

    @AfterAll
    static void tearDownPlaywright() {
        if (browser != null) {
            browser.close();
            browser = null;
        }
        if (playwright != null) {
            playwright.close();
            playwright = null;
        }
    }

    private static boolean isHeadless() {
        return Optional.ofNullable(System.getenv("HEADLESS"))
                .map(v -> Boolean.parseBoolean(v))
                .orElseGet(() -> System.getenv("CI") != null);
    }

    private static String browserName() {
        return Optional.ofNullable(System.getenv("BROWSER"))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(v -> !v.isEmpty())
                .orElse("chromium");
    }
}
