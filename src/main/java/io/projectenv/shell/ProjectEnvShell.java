package io.projectenv.shell;

import io.projectenv.core.cli.api.ToolInfo;
import io.projectenv.core.cli.api.ToolInfoParser;
import io.projectenv.core.commons.process.ProcessEnvironmentHelper;
import io.projectenv.core.commons.process.ProcessHelper;
import io.projectenv.core.commons.process.ProcessOutputWriterAccessor;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "project-env-shell")
public final class ProjectEnvShell implements Callable<Integer> {

    private static final String PROJECT_ENV_CLI_NAME = "project-env-cli";

    @Option(names = {"--project-root"}, defaultValue = ".")
    protected File projectRoot;

    @Option(names = {"--config-file"}, required = true)
    protected File configFile;

    @Option(names = {"--output-template"}, required = true)
    private String outputTemplate;

    @Option(names = {"--output-file"}, required = true)
    private File outputFile;

    @Option(names = {"--debug"})
    private boolean debug;

    @Override
    public Integer call() throws ProjectEnvShellException {
        if (!configFile.exists()) {
            throw new ProjectEnvShellException("config file not found");
        }

        var projectEnvCliExecutable = ProcessEnvironmentHelper.resolveExecutableFromPath(PROJECT_ENV_CLI_NAME);
        if (projectEnvCliExecutable == null || !projectEnvCliExecutable.exists()) {
            throw new ProjectEnvShellException("cannot resolve Project-Env CLI from PATH variable");
        }

        var rawToolInfos = executeProjectEnvCli(projectEnvCliExecutable, configFile);
        if (debug) {
            ProcessOutputWriterAccessor.getProcessInfoWriter().write("resulting tool infos: " + rawToolInfos);
        }

        var toolInfos = ToolInfoParser.fromJson(rawToolInfos);
        writeOutput(toolInfos);

        return 0;
    }

    private String executeProjectEnvCli(File projectEnvCliExecutable, File configurationFile) throws ProjectEnvShellException {
        try {
            var processBuilder = new ProcessBuilder()
                    .command(
                            projectEnvCliExecutable.getCanonicalPath(),
                            "--project-root",
                            projectRoot.getCanonicalPath(),
                            "--config-file",
                            configurationFile.getCanonicalPath()
                    )
                    .directory(projectRoot);

            var processResult = ProcessHelper.executeProcess(processBuilder, true);
            if (processResult.getExitCode() != 0) {
                throw new ProjectEnvShellException("Project-Env CLI exited with a non zero exit code");
            }

            return processResult.getOutput().orElse(StringUtils.EMPTY);
        } catch (IOException e) {
            throw new ProjectEnvShellException("failed to execute Project-Env CLI", e);
        }
    }

    private void writeOutput(Map<String, List<ToolInfo>> toolInfos) throws ProjectEnvShellException {
        try {
            String content = TemplateProcessor.processTemplate(outputTemplate, toolInfos);
            FileUtils.write(outputFile, content, StandardCharsets.UTF_8);

            if (!SystemUtils.IS_OS_WINDOWS && !outputFile.setExecutable(true)) {
                ProcessOutputWriterAccessor.getProcessInfoWriter().write("failed to make file {0} executable", outputFile.getCanonicalPath());
            }
        } catch (IOException e) {
            throw new ProjectEnvShellException("failed to execute Project-Env CLI", e);
        }
    }

    public static void main(String[] args) {
        System.exit(executeProjectEnvShell(args));
    }

    protected static int executeProjectEnvShell(String[] args) {
        return new CommandLine(new ProjectEnvShell()).execute(args);
    }

}
