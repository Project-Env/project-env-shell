package io.projectenv.shell;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

class ProjectEnvShellTest extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(String path, String... params) throws Exception {
        withEnvironmentVariable("PATH", path).execute(() -> {
            ProjectEnvShell.executeProjectEnvShell(params);
        });
    }

}
