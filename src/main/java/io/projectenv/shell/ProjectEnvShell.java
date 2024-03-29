package io.projectenv.shell;

import io.projectenv.core.commons.process.ProcessEnvironmentHelper;
import io.projectenv.core.commons.process.ProcessHelper;
import io.projectenv.core.commons.process.ProcessOutput;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    @Option(names = {"--output-file"})
    private File outputFile;

    @Option(names = {"--debug"})
    private boolean debug;

    @Override
    public Integer call() {
        try {
            if (debug) {
                ProcessOutput.activateDebugMode();
            }

            if (!configFile.exists()) {
                return handleError("config file {0} not found", configFile);
            }

            var projectEnvCliExecutable = ProcessEnvironmentHelper.resolveExecutableFromPath(PROJECT_ENV_CLI_NAME);
            if (projectEnvCliExecutable == null) {
                return handleError("cannot resolve Project-Env CLI from PATH variable");
            }

            var rawToolInfos = executeProjectEnvCli(projectEnvCliExecutable, configFile);
            ProcessOutput.writeDebugMessage("resulting tool infos: {0}", rawToolInfos);

            var toolInfos = ToolInfoParser.fromJson(rawToolInfos);
            writeOutput(toolInfos);

            return 0;
        } catch (Exception e) {
            ProcessOutput.writeDebugMessage(e);
            return handleError("failed to execute shell: {0}", ExceptionUtils.getRootCauseMessage(e));
        }
    }

    private Integer handleError(String outputPattern, Object... outputArgs) {
        ProcessOutput.writeInfoMessage(outputPattern, outputArgs);

        return CommandLine.ExitCode.SOFTWARE;
    }

    private String executeProjectEnvCli(File projectEnvCliExecutable, File configurationFile) throws IOException {
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

        return processResult.getStdOutput().orElse(StringUtils.EMPTY);
    }

    private void writeOutput(Map<String, List<ToolInfo>> toolInfos) throws IOException {
        String content = TemplateProcessor.processTemplate(outputTemplate, toolInfos);

        if (outputFile != null) {
            writeOutputToFile(content, outputFile);
        } else {
            writeOutputToStdOutput(content);
        }
    }

    private void writeOutputToFile(String content, File target) throws IOException {
        FileUtils.write(target, content, StandardCharsets.UTF_8);

        if (!SystemUtils.IS_OS_WINDOWS && !target.setExecutable(true)) {
            ProcessOutput.writeInfoMessage("failed to make file {0} executable", target.getCanonicalPath());
        }
    }

    private void writeOutputToStdOutput(String content) {
        ProcessOutput.writeResult(content);
    }

    public static void main(String[] args) {
        System.exit(executeProjectEnvShell(args));
    }

    protected static int executeProjectEnvShell(String[] args) {
        return new CommandLine(new ProjectEnvShell()).execute(args);
    }

}
