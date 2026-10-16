package io.github.lihuu.javacli.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

class CliApplicationTest {
    @Test
    void helloPrintsGreetingToStdout() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello"}, new PrintWriter(out, true), new PrintWriter(err, true));
        assertEquals(0, code);
        assertEquals("Hello, world!" + System.lineSeparator(), out.toString());
        assertEquals("", err.toString());
    }

    @Test
    void bareInvocationPrintsHelp() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[0], new PrintWriter(out, true), new PrintWriter(new StringWriter(), true));
        assertEquals(0, code);
        assertTrue(out.toString().contains("Usage:") && out.toString().contains("hello"));
    }


    @Test
    void helloAcceptsName() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello", "--name", "Ada"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true));
        assertEquals(0, code);
        assertEquals("Hello, Ada!" + System.lineSeparator(), out.toString());
    }


    @Test
    void versionUsesFilteredProjectVersion() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = CliApplicationTest.class.getResourceAsStream("/application.properties")) {
            assertNotNull(input);
            properties.load(input);
        }
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"--version"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true));
        assertEquals(0, code);
        assertEquals(properties.getProperty("application.name") + " " + properties.getProperty("application.version") + System.lineSeparator(), out.toString());
        assertFalse(out.toString().contains("${"));
    }


    @Test
    void interactiveGreetingUsesColor() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true), Map.of("TERM", "xterm-256color"), true);
        assertEquals(0, code);
        assertEquals("\u001b[32mHello, world!\u001b[0m" + System.lineSeparator(), out.toString());
    }


    @Test
    void explicitHelpDoesNotRunGreeting() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"--help"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true));
        assertEquals(0, code);
        assertTrue(out.toString().contains("Usage:"));
        assertFalse(out.toString().contains("Hello, world!"));
    }

    @Test
    void invalidArgumentsGoToStderrWithUsageExitCode() {
        StringWriter out = new StringWriter();
        StringWriter err = new StringWriter();
        int code = CliApplication.execute(new String[] {"--unknown"}, new PrintWriter(out, true), new PrintWriter(err, true));
        assertEquals(2, code);
        assertEquals("", out.toString());
        assertTrue(err.toString().contains("Unknown option"));
        assertFalse(err.toString().contains("\u001b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"root", "child"})
    void noColorWorksBeforeAndAfterSubcommand(String position) {
        String[] args = position.equals("root") ? new String[] {"--no-color", "hello"} : new String[] {"hello", "--no-color"};
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(args, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true), Map.of("TERM", "xterm"), true);
        assertEquals(0, code);
        assertEquals("Hello, world!" + System.lineSeparator(), out.toString());
    }

    @Test
    void emptyNoColorEnvironmentDisablesColor() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true), Map.of("TERM", "xterm", "NO_COLOR", ""), true);
        assertEquals(0, code);
        assertEquals("Hello, world!" + System.lineSeparator(), out.toString());
    }

    @Test
    void redirectedOutputHasNoColor() {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true), Map.of("TERM", "xterm"), false);
        assertEquals(0, code);
        assertEquals("Hello, world!" + System.lineSeparator(), out.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"dumb", ""})
    void incapableTerminalHasNoColor(String terminal) {
        StringWriter out = new StringWriter();
        int code = CliApplication.execute(new String[] {"hello"}, new PrintWriter(out, true), new PrintWriter(new StringWriter(), true), Map.of("TERM", terminal), true);
        assertEquals(0, code);
        assertEquals("Hello, world!" + System.lineSeparator(), out.toString());
    }
}
