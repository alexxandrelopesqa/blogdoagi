package tests;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.LoadState;
import core.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import pages.BlogHomePage;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Smoke")
@Feature("Sanidade")
@Tag("smoke")
@Tag("regression")
class BlogSmokeTest extends BaseTest {

    @Test
    @Story("Home")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Home responde, título identificável e área principal visível.")
    @DisplayName("SMK-01 — home carrega")
    void home_carregaTituloEMain() {
        try {
            new BlogHomePage(page).navigate();
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String title = tituloComRetry();
            Assertions.assertFalse(title.isBlank(), "Título da página não deve ser vazio");
            Assertions.assertTrue(
                    title.toLowerCase().contains("agi") || title.toLowerCase().contains("blog"),
                    "Título deve referenciar o blog Agi: " + title);
            assertThat(page.locator("main#primary, main, #primary, .site-content").first()).isVisible();
        } finally {
            finalizeEvidenceInTestContext("smk-home");
        }
    }

    private String tituloComRetry() {
        PlaywrightException last = null;
        for (int i = 0; i < 4; i++) {
            try {
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);
                return page.title();
            } catch (PlaywrightException e) {
                last = e;
                if (!e.getMessage().contains("Execution context was destroyed")) {
                    throw e;
                }
                page.waitForTimeout(500);
            }
        }
        throw last;
    }
}
