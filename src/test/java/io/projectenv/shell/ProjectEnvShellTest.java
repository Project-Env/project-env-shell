package io.projectenv.shell;

import io.projectenv.core.commons.process.ProcessOutputWriter;
import io.projectenv.core.commons.process.ProcessOutputWriterAccessor;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import picocli.CommandLine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellTest extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(File pathElement, String... params) throws Exception {
        withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute(() -> {
            ProjectEnvShell.executeProjectEnvShell(params);
        });
    }

    @Test
    void testConfigFileNotExisting(@TempDir File tempDir) {
        try (var processOutputWriterAccessor = Mockito.mockStatic(ProcessOutputWriterAccessor.class)) {
            var processOutputWriter = new CollectingProcessOutputWriter();
            processOutputWriterAccessor.when(ProcessOutputWriterAccessor::getProcessInfoWriter).thenReturn(processOutputWriter);

            var projectEnvShell = new ProjectEnvShell();
            projectEnvShell.configFile = new File("any file");
            projectEnvShell.projectRoot = tempDir;

            assertThat(projectEnvShell.call()).isEqualTo(CommandLine.ExitCode.SOFTWARE);
            assertThat(processOutputWriter.OUTPUT_LINES).containsExactly("config file any file not found");
        }
    }

    @Test
    void testNotExistingCli(@TempDir File tempDir) throws Exception {
        withEnvironmentVariable("PATH", "empty").execute(() -> {
            try (var processOutputWriterAccessor = Mockito.mockStatic(ProcessOutputWriterAccessor.class)) {
                var processOutputWriter = new CollectingProcessOutputWriter();
                processOutputWriterAccessor.when(ProcessOutputWriterAccessor::getProcessInfoWriter).thenReturn(processOutputWriter);

                var configFile = new File(tempDir, "project-env.toml");
                FileUtils.touch(configFile);

                var projectEnvShell = new ProjectEnvShell();
                projectEnvShell.configFile = configFile;
                projectEnvShell.projectRoot = tempDir;

                assertThat(projectEnvShell.call()).isEqualTo(CommandLine.ExitCode.SOFTWARE);
                assertThat(processOutputWriter.OUTPUT_LINES).containsExactly("cannot resolve Project-Env CLI from PATH variable");
            }
        });
    }

    private static class CollectingProcessOutputWriter implements ProcessOutputWriter {

        private final List<String> OUTPUT_LINES = new ArrayList<>();

        @Override
        public void write(String line) {
            OUTPUT_LINES.add(line);
        }

    }

}
