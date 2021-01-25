package io.projectenv.shell;

import io.projectenv.core.configuration.ProjectEnvConfiguration;
import io.projectenv.core.configuration.ProjectEnvConfigurationFactory;
import io.projectenv.core.installer.ToolInstallers;
import io.projectenv.core.toolinfo.ToolInfo;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "project-env-shell")
public class ProjectEnvShell implements Callable<Integer> {

    @Option(names = {"--config-file"}, required = true)
    private File configFile;

    @Option(names = {"--output-template"}, required = true)
    private String outputTemplate;

    @Option(names = {"--output-file"}, required = true)
    private File outputFile;

    public static void main(String[] args) throws Exception {
        int exitCode = new CommandLine(new ProjectEnvShell()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() throws Exception {
        ProjectEnvConfiguration projectEnvConfiguration = ProjectEnvConfigurationFactory.createFromFile(configFile);

        List<ToolInfo> toolInfos = ToolInstallers.installAllTools(projectEnvConfiguration, new File("."));

        writeOutput(toolInfos);

        return 0;
    }

    private void writeOutput(List<ToolInfo> toolInfos) throws Exception {
        String content = TemplateProcessor.processTemplate(outputTemplate, toolInfos);

        FileUtils.write(outputFile, content, StandardCharsets.UTF_8);
        outputFile.setExecutable(true);
    }

}
