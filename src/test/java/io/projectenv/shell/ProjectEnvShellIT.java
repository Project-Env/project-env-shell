package io.projectenv.shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellIT extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(String... params) throws Exception {
        List<String> commands = new ArrayList<>();
        commands.add("./target/project-env-shell");
        commands.addAll(Arrays.asList(params));

        Process process = new ProcessBuilder(commands).inheritIO().start();

        boolean terminated = process.waitFor(5, TimeUnit.MINUTES);
        if (!terminated) {
            process.destroy();
        }
        assertThat(process.exitValue()).describedAs("process exit code").isZero();
    }

}
