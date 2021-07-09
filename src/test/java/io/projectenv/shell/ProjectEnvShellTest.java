package io.projectenv.shell;

import java.io.File;

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.createExtendedPathValue;
import static io.projectenv.core.commons.process.ProcessEnvironmentHelper.getPathVariableName;

class ProjectEnvShellTest extends AbstractProjectEnvShellTest {

    @Override
    protected void executeProjectEnvShell(File pathElement, String... params) throws Exception {
        withEnvironmentVariable(getPathVariableName(), createExtendedPathValue(pathElement)).execute(() -> {
            ProjectEnvShell.executeProjectEnvShell(params);
        });
    }

}
