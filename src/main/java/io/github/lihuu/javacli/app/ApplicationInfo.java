package io.github.lihuu.javacli.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.jetbrains.annotations.NotNull;

record ApplicationInfo(@NotNull String name, @NotNull String version) {
    static @NotNull ApplicationInfo load() {
        try (InputStream input = ApplicationInfo.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("Missing /application.properties");
            }
            Properties properties = new Properties();
            properties.load(input);
            String name = properties.getProperty("application.name");
            String version = properties.getProperty("application.version");
            if (name == null || name.isBlank() || version == null || version.isBlank()) {
                throw new IllegalStateException("Missing application name or version");
            }
            return new ApplicationInfo(name, version);
        } catch (IOException failure) {
            throw new IllegalStateException("Unable to read /application.properties", failure);
        }
    }
}
