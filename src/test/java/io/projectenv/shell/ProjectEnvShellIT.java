package io.projectenv.shell;

import io.projectenv.core.commons.process.ProcessHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellIT extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(String path, String... params) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add("./target/project-env-shell");
        commands.addAll(Arrays.asList(params));

        var processBuilder = new ProcessBuilder(commands);
        processBuilder.environment().put("PATH", path);
        Process process = processBuilder.start();

        ProcessHelper.bindErrOutput(process);

        boolean terminated = process.waitFor(5, TimeUnit.MINUTES);
        if (!terminated) {
            process.destroy();
        }


        assertThat(process.exitValue()).describedAs("process exit code").isZero();
    }

}
