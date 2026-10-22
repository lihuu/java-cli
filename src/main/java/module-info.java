module io.github.lihuu.javacli {
    requires info.picocli;
    requires static org.jetbrains.annotations;

    opens io.github.lihuu.javacli.cli to info.picocli;
    opens io.github.lihuu.javacli.app to info.picocli;

    exports io.github.lihuu.javacli.app;
    exports io.github.lihuu.javacli.cli;
}
