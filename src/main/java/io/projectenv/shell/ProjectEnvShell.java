package io.projectenv.shell;

import io.projectenv.core.cli.api.ToolInfo;
import io.projectenv.core.cli.api.ToolInfoParser;
import io.projectenv.core.commons.process.ProcessHelper;
import io.projectenv.core.commons.process.ProcessOutputWriterAccessor;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Command(name = "project-env-shell")
public final class ProjectEnvShell implements Callable<Integer> {

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

        var projectEnvCliExecutable = getProjectEnvCliExecutable();
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

    private File getProjectEnvCliExecutable() {
        var projectEnvCliExecutableName = getProjectEnvCliExecutableName();
        for (String pathElement : System.getenv().get("PATH").split(File.pathSeparator)) {
            var projectEnvCliExecutable = new File(pathElement, projectEnvCliExecutableName);
            if (projectEnvCliExecutable.exists()) {
                return projectEnvCliExecutable;
            }
        }

        return null;
    }

    private String getProjectEnvCliExecutableName() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "project-env-cli.exe";
        } else {
            return "project-env-cli";
        }
    }

    private String executeProjectEnvCli(File projectEnvCliExecutable, File configurationFile) throws ProjectEnvShellException {
        try {
            var process = new ProcessBuilder()
                    .command(
                            projectEnvCliExecutable.getCanonicalPath(),
                            "--project-root",
                            projectRoot.getCanonicalPath(),
                            "--config-file",
                            configurationFile.getCanonicalPath()
                    )
                    .directory(projectRoot)
                    .start();

            ProcessHelper.bindErrOutput(process);

            var rawResultBuilder = new StringBuilder();
            var rawResultReaderThread = new Thread(() -> {
                try (var scanner = new Scanner(process.getInputStream())) {
                    while (scanner.hasNextLine()) {
                        rawResultBuilder.append(scanner.nextLine()).append('\n');
                    }
                }
            });
            rawResultReaderThread.start();

            waitForProjectEnvCliProcessTermination(process);
            waitForRawResultReaderThreadTermination(rawResultReaderThread);

            if (process.exitValue() != 0) {
                throw new ProjectEnvShellException("Project-Env CLI exited with a non zero exit code");
            }

            return rawResultBuilder.toString();
        } catch (IOException e) {
            throw new ProjectEnvShellException("failed to execute Project-Env CLI", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProjectEnvShellException("interrupted while waiting for Project-Env CLI termination", e);
        }
    }

    private void waitForProjectEnvCliProcessTermination(Process projectEnvCliProcess) throws InterruptedException {
        if (!projectEnvCliProcess.waitFor(1, TimeUnit.HOURS)) {
            projectEnvCliProcess.destroy();
        }
    }

    private void waitForRawResultReaderThreadTermination(Thread rawResultReaderThread) throws InterruptedException {
        rawResultReaderThread.join(TimeUnit.MINUTES.toMillis(1));
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
