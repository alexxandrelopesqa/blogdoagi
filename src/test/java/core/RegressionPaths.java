package core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/** URLs de regressão a partir de {@code regression.properties} + {@link BaseTest#BASE_URL}. */
public final class RegressionPaths {

    private static final Properties PROPS = load();

    private RegressionPaths() {}

    private static Properties load() {
        Properties p = new Properties();
        try (InputStream in = RegressionPaths.class.getResourceAsStream("/regression.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException("Falha ao carregar regression.properties", e);
        }
        return p;
    }

    public static String absoluteUrl(String propertyKey) {
        String raw = Objects.requireNonNull(PROPS.getProperty(propertyKey), "Chave ausente: " + propertyKey)
                .trim();
        if (raw.isEmpty()) {
            throw new IllegalStateException("Valor vazio para: " + propertyKey);
        }
        if (raw.startsWith("http://") || raw.startsWith("https://")) {
            return raw;
        }
        String path = raw.startsWith("/") ? raw : "/" + raw;
        String base = BaseTest.BASE_URL.replaceAll("/$", "");
        return base + path;
    }
}
