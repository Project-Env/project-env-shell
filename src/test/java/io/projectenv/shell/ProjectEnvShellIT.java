package io.projectenv.shell;

import io.projectenv.core.commons.process.ProcessHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName;
import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellIT extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(File pathElement, String... params) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add("./target/project-env-shell");
        commands.addAll(Arrays.asList(params));

        var processBuilder = new ProcessBuilder(commands);
        processBuilder.environment().put(getPathVariableName(), createExtendedPathValue(pathElement));

        var exitValue = ProcessHelper.executeProcess(processBuilder);
        assertThat(exitValue).describedAs("process exit code").isZero();
    }

}
