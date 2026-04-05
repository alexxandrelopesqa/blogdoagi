package core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Ciclo de vida do Playwright: um browser por suíte, contexto isolado por teste.
 * <p>
 * Headless: variável {@code HEADLESS} ({@code true}/{@code false}). Se ausente,
 * usa headless quando {@code CI} estiver definido (por exemplo, GitHub Actions).
 */
public abstract class BaseTest {

    public static final String BASE_URL = resolveBaseUrl();
    private static final Path ALLURE_RESULTS_DIR = Paths.get("target", "allure-results");
    private static final Path VIDEOS_DIR = Paths.get("target", "artifacts", "videos");
    private static final Path TRACES_DIR = Paths.get("target", "artifacts", "traces");

    private static Playwright playwright;
    private static Browser browser;
    private static final AtomicInteger TEST_COUNTER = new AtomicInteger(0);

    protected com.microsoft.playwright.Page page;
    private com.microsoft.playwright.BrowserContext context;
    private StringBuilder runtimeLogs;

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
        writeAllureMetadataFiles();
    }

    @BeforeEach
    void createPage(TestInfo testInfo) {
        createArtifactsDirs();
        context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(VIDEOS_DIR));
        context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions()
                .setName(safeTestName(testInfo))
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));

        page = context.newPage();
        runtimeLogs = new StringBuilder();
        bindRuntimeLogListeners();
    }

    @AfterEach
    void closePageFallback() {
        closeContextSafely();
        page = null;
        context = null;
        runtimeLogs = null;
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

    private static String resolveBaseUrl() {
        return Optional.ofNullable(System.getProperty("base.url"))
                .or(() -> Optional.ofNullable(System.getenv("BASE_URL")))
                .map(String::trim)
                .filter(v -> !v.isEmpty())
                .orElse("https://blog.agibank.com.br");
    }

    private static void createArtifactsDirs() {
        try {
            Files.createDirectories(ALLURE_RESULTS_DIR);
            Files.createDirectories(VIDEOS_DIR);
            Files.createDirectories(TRACES_DIR);
        } catch (IOException e) {
            throw new RuntimeException("Falha ao criar diretórios de artefatos.", e);
        }
    }

    private static void writeAllureMetadataFiles() {
        createArtifactsDirs();
        writeEnvironmentProperties();
        writeExecutorJson();
        copyCategoriesFile();
    }

    private static void writeEnvironmentProperties() {
        Properties env = new Properties();
        env.setProperty("Application", "Blog do Agi");
        env.setProperty("Base URL", BASE_URL);
        env.setProperty("Browser", browserName());
        env.setProperty("Headless", String.valueOf(isHeadless()));
        env.setProperty("CI", String.valueOf(System.getenv("CI") != null));
        env.setProperty("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        env.setProperty("Java", System.getProperty("java.version"));

        Path file = ALLURE_RESULTS_DIR.resolve("environment.properties");
        try (var out = Files.newOutputStream(file)) {
            env.store(out, "Allure environment metadata");
        } catch (IOException e) {
            System.err.println("Falha ao gerar environment.properties do Allure: " + e.getMessage());
        }
    }

    private static void writeExecutorJson() {
        String runId = Optional.ofNullable(System.getenv("GITHUB_RUN_ID")).orElse("local");
        String buildUrl = Optional.ofNullable(System.getenv("GITHUB_SERVER_URL"))
                .flatMap(server -> Optional.ofNullable(System.getenv("GITHUB_REPOSITORY"))
                        .flatMap(repo -> Optional.ofNullable(System.getenv("GITHUB_RUN_ID"))
                                .map(id -> server + "/" + repo + "/actions/runs/" + id)))
                .orElse("");
        String buildName = Optional.ofNullable(System.getenv("GITHUB_WORKFLOW")).orElse("Local execution");

        String json = "{\n"
                + "  \"name\": \"GitHub Actions\",\n"
                + "  \"type\": \"github\",\n"
                + "  \"buildName\": \"" + escapeJson(buildName) + "\",\n"
                + "  \"buildOrder\": " + "\"" + escapeJson(runId) + "\",\n"
                + "  \"buildUrl\": \"" + escapeJson(buildUrl) + "\",\n"
                + "  \"reportName\": \"Allure Report\",\n"
                + "  \"reportUrl\": \"\"\n"
                + "}";
        try {
            Files.writeString(ALLURE_RESULTS_DIR.resolve("executor.json"), json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Falha ao gerar executor.json do Allure: " + e.getMessage());
        }
    }

    private static void copyCategoriesFile() {
        Path target = ALLURE_RESULTS_DIR.resolve("categories.json");
        try (InputStream in = BaseTest.class.getResourceAsStream("/allure/categories.json")) {
            if (in == null) {
                return;
            }
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Falha ao copiar categories.json do Allure: " + e.getMessage());
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void bindRuntimeLogListeners() {
        page.onConsoleMessage(msg -> {
            runtimeLogs.append("[CONSOLE] ")
                    .append(msg.type())
                    .append(" - ")
                    .append(msg.text())
                    .append(System.lineSeparator());
        });
        page.onPageError(err -> runtimeLogs.append("[PAGE_ERROR] ")
                .append(err)
                .append(System.lineSeparator()));
        page.onRequestFailed(req -> runtimeLogs.append("[REQUEST_FAILED] ")
                .append(req.method())
                .append(" ")
                .append(req.url())
                .append(" -> ")
                .append(req.failure() != null ? req.failure() : "unknown")
                .append(System.lineSeparator()));
    }

    private String safeTestName(TestInfo testInfo) {
        String raw = testInfo.getDisplayName() + "-" + TEST_COUNTER.incrementAndGet();
        return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String safeTestName(String displayName) {
        String raw = displayName + "-" + TEST_COUNTER.incrementAndGet();
        return raw.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Deve ser chamado no bloco {@code finally} do teste para anexar evidências no contexto ativo do Allure.
     */
    protected void finalizeEvidenceInTestContext(String displayName) {
        String artifactBaseName = safeTestName(displayName);
        try {
            attachScreenshot(artifactBaseName + "-screenshot");
            attachRuntimeLogs(artifactBaseName + "-runtime-logs");
            stopAndAttachTrace(artifactBaseName + "-trace.zip");
        } finally {
            Path videoPath = captureVideoPathIfAny();
            closeContextSafely();
            attachVideoIfAny(artifactBaseName + "-video.webm", videoPath);
            page = null;
            context = null;
            runtimeLogs = null;
        }
    }

    private void attachScreenshot(String name) {
        if (page == null) {
            return;
        }
        byte[] screenshot = page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
        Allure.addAttachment(name, "image/png", new ByteArrayInputStream(screenshot), "png");
    }

    private void attachRuntimeLogs(String name) {
        if (runtimeLogs == null || runtimeLogs.isEmpty()) {
            runtimeLogs = new StringBuilder("[INFO] Nenhum log de runtime capturado.");
        }
        Allure.addAttachment(name, "text/plain", runtimeLogs.toString());
    }

    private void stopAndAttachTrace(String fileName) {
        if (context == null) {
            return;
        }
        Path tracePath = TRACES_DIR.resolve(fileName);
        context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions().setPath(tracePath));
        attachFileIfExists(fileName, tracePath, "application/zip");
    }

    private Path captureVideoPathIfAny() {
        try {
            if (page != null && page.video() != null) {
                return page.video().path();
            }
        } catch (Exception ignored) {
            // Em alguns casos o path só está disponível após o close do context.
        }
        return null;
    }

    private void closeContextSafely() {
        if (context != null) {
            context.close();
        }
    }

    private void attachVideoIfAny(String fileName, Path videoPath) {
        if (videoPath != null) {
            attachFileIfExists(fileName, videoPath, "video/webm");
            return;
        }
        try {
            if (page != null && page.video() != null) {
                Path delayedPath = page.video().path();
                attachFileIfExists(fileName, delayedPath, "video/webm");
            }
        } catch (Exception ignored) {
            // Se vídeo não estiver disponível, segue sem falhar a suíte.
        }
    }

    private void attachFileIfExists(String name, Path path, String contentType) {
        try {
            if (path != null && Files.exists(path)) {
                byte[] data = Files.readAllBytes(path);
                if ("video/webm".equals(contentType)) {
                    Allure.addAttachment(name, contentType, new ByteArrayInputStream(data), "webm");
                } else if ("application/zip".equals(contentType)) {
                    Allure.addAttachment(name, contentType, new ByteArrayInputStream(data), "zip");
                } else {
                    Allure.addAttachment(name, contentType, new ByteArrayInputStream(data), "bin");
                }
            }
        } catch (Exception ignored) {
            // Não deve quebrar o teardown por falha de anexo.
        }
    }
}
