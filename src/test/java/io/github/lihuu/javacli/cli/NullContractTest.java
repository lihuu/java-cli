package io.github.lihuu.javacli.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NullContractTest {
    @Test
    void annotatedConstructorRejectsNullBeforeRunningCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> new HelloCommand(null, new CliCommand(Map.of(), false)));
    }

    @Test
    void annotatedConstructorRejectsMissingRoot() {
        assertThrows(IllegalArgumentException.class,
                () -> new HelloCommand(new PrintWriter(new StringWriter()), null));
    }
}
