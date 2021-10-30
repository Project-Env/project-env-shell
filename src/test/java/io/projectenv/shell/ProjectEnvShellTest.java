package io.projectenv.shell;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellTest extends AbstractProjectEnvShellTest {

    @Override
    protected String executeProjectEnvShell(File pathElement, String... params) throws Exception {
        var originalStream = System.out;
        try (var outputStream = new ByteArrayOutputStream()) {
            System.setOut(new PrintStream(outputStream));

            withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute(() -> {
                ProjectEnvShell.executeProjectEnvShell(params);
            });

            return outputStream.toString(StandardCharsets.UTF_8);
        } finally {
            System.setOut(originalStream);
        }
    }

    @Test
    void testConfigFileNotExisting(@TempDir File tempDir) throws Exception {
        var originalStream = System.out;
        try (var outputStream = new ByteArrayOutputStream()) {
            System.setErr(new PrintStream(outputStream));

            var projectEnvShell = new ProjectEnvShell();
            projectEnvShell.configFile = new File("any file");
            projectEnvShell.projectRoot = tempDir;

            assertThat(projectEnvShell.call()).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(outputStream.toString(StandardCharsets.UTF_8)).isEqualToIgnoringNewLines("config file any file not found");
        } finally {
            System.setOut(originalStream);
        }
    }

    @Test
    void testNotExistingCli(@TempDir File tempDir) throws Exception {
        withEnvironmentVariable("PATH", "empty").execute(() -> {
            var originalStream = System.out;
            try (var outputStream = new ByteArrayOutputStream()) {
                System.setErr(new PrintStream(outputStream));

                var configFile = new File(tempDir, "project-env.toml");
                FileUtils.touch(configFile);

                var projectEnvShell = new ProjectEnvShell();
                projectEnvShell.configFile = configFile;
                projectEnvShell.projectRoot = tempDir;

                assertThat(projectEnvShell.call()).isEqualTo(CommandLine.ExitCode.SOFTWARE);
                assertThat(outputStream.toString(StandardCharsets.UTF_8)).isEqualToIgnoringNewLines("cannot resolve Project-Env CLI from PATH variable");
            } finally {
                System.setOut(originalStream);
            }
        });
    }

}
