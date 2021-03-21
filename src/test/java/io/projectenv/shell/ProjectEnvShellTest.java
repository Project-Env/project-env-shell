package io.projectenv.shell;

class ProjectEnvShellTest extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(String... params) throws Exception {
        ProjectEnvShell.executeProjectEnvShell(params);
    }

}
