package tests;

import core.BaseTest;
import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pages.BlogHomePage;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

@Epic("Layout")
@Feature("Responsividade")
class BlogResponsiveLayoutTest extends BaseTest {

    /** Larguras comuns: mobile, tablet, laptop e desktop (altura típica por faixa). */
    static Stream<Arguments> viewports() {
        return Stream.of(
                Arguments.of("mobile-320", 320, 568),
                Arguments.of("mobile-375", 375, 667),
                Arguments.of("mobile-414", 414, 896),
                Arguments.of("tablet-768", 768, 1024),
                Arguments.of("tablet-1024", 1024, 768),
                Arguments.of("laptop-1280", 1280, 720),
                Arguments.of("desktop-1920", 1920, 1080));
    }

    @ParameterizedTest(name = "{0} — {1}×{2}")
    @MethodSource("viewports")
    @Story("Viewport")
    @Owner("alexxandrelopesqa")
    @Severity(SeverityLevel.NORMAL)
    @Description("Home carrega, área principal visível, sem overflow horizontal gritante, busca acessível e fluxo ?s= funciona.")
    void home_responsiva_layoutEBusca(String label, int width, int height) {
        try {
            page.setViewportSize(width, height);
            Allure.parameter("viewport", label);
            Allure.parameter("width", String.valueOf(width));
            Allure.parameter("height", String.valueOf(height));

            var home = new BlogHomePage(page).navigate();

            Allure.step("Área principal visível");
            assertThat(page.locator("main#primary, main, #primary, .site-content").first()).isVisible();

            Allure.step("Largura do documento vs viewport (estrita ≤1024px; desktop ignora micro-overflow do tema)");
            if (width <= 1024) {
                Object overflowCheck = page.evaluate(
                        "() => { const iw = window.innerWidth; const sw = document.documentElement.scrollWidth; "
                                + "return sw <= iw + 24; }");
                Assertions.assertTrue(
                        Boolean.TRUE.equals(overflowCheck),
                        "Possível overflow horizontal em " + label + " (" + width + "×" + height + ")");
            }

            Allure.step("Busca funciona (UI ou fallback por URL ?s=)");
            home.search("Agi");
            assertThat(page).hasURL(Pattern.compile(".*[?&]s=Agi.*", Pattern.CASE_INSENSITIVE));
        } finally {
            finalizeEvidenceInTestContext("responsive-" + label);
        }
    }
}
