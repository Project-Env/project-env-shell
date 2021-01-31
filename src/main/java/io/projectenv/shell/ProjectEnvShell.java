package io.projectenv.shell;

import io.projectenv.core.common.OperatingSystem;
import io.projectenv.core.configuration.ProjectEnvConfiguration;
import io.projectenv.core.configuration.ProjectEnvConfigurationFactory;
import io.projectenv.core.installer.ToolInstallers;
import io.projectenv.core.toolinfo.ToolInfo;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "project-env-shell")
public class ProjectEnvShell implements Callable<Integer> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Option(names = {"--config-file"}, required = true)
    private File configFile;

    @Option(names = {"--output-template"}, required = true)
    private String outputTemplate;

    @Option(names = {"--output-file"}, required = true)
    private File outputFile;

    @Option(names = {"--project-root"}, defaultValue = ".")
    private File projectRoot;

    public static void main(String[] args) {
        new CommandLine(new ProjectEnvShell()).execute(args);
    }

    @Override
    public Integer call() throws Exception {
        ProjectEnvConfiguration projectEnvConfiguration = ProjectEnvConfigurationFactory.createFromFile(configFile);

        List<ToolInfo> toolInfos = ToolInstallers.installAllTools(projectEnvConfiguration, projectRoot);

        writeOutput(toolInfos);

        return 0;
    }

    private void writeOutput(List<ToolInfo> toolInfos) throws IOException {
        String content = TemplateProcessor.processTemplate(outputTemplate, toolInfos);

        FileUtils.write(outputFile, content, StandardCharsets.UTF_8);

        if (OperatingSystem.getCurrentOS() != OperatingSystem.WINDOWS && !outputFile.setExecutable(true)) {
            log.warn("failed to make file '{}' executable", outputFile.getCanonicalPath());
        }
    }

}
