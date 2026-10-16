package io.github.lihuu.javacli;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** Lightweight explicit-type guard, not a complete Java parser. */
class SourceConventionsTest {
    private static final Pattern VAR_DECLARATION = Pattern.compile(
            "^\\s*(?:final\\s+)?var\\s+[A-Za-z_$]");

    @Test
    void javaSourcesUseExplicitLocalTypes() throws IOException {
        List<String> offenders = new ArrayList<>();
        for (Path source : sources()) {
            List<String> lines = Files.readAllLines(source);
            for (int i = 0; i < lines.size(); i++) {
                if (VAR_DECLARATION.matcher(lines.get(i)).find()) {
                    offenders.add(source + ":" + (i + 1));
                }
            }
        }
        assertTrue(offenders.isEmpty(), "Use explicit local types: " + offenders);
    }

    private static List<Path> sources() throws IOException {
        Path root = Path.of("src");
        assertTrue(Files.isDirectory(root), "Run tests from the project root");
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }
}
