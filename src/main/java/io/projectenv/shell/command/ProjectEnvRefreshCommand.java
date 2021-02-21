package io.projectenv.shell.command;


import io.projectenv.core.common.OperatingSystem;
import io.projectenv.core.configuration.ToolsConfiguration;
import io.projectenv.core.tools.info.ToolInfo;
import io.projectenv.core.tools.repository.ToolsRepository;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.io.FileUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Command(name = "refresh")
public class ProjectEnvRefreshCommand extends AbstractProjectEnvCommand {

    @Option(names = {"--output-template"}, required = true)
    private String outputTemplate;

    @Option(names = {"--output-file"}, required = true)
    private File outputFile;

    @Override
    protected void executeCommand(ToolsConfiguration configuration, ToolsRepository repository) throws Exception {
        List<ToolInfo> toolInfos = repository.requestTools(configuration.getAllToolConfigurations(), projectRoot);
        writeOutput(toolInfos);
    }

    private void writeOutput(List<ToolInfo> toolInfos) throws IOException {
        String content = TemplateProcessor.processTemplate(outputTemplate, toolInfos);

        FileUtils.write(outputFile, content, StandardCharsets.UTF_8);

        if (OperatingSystem.getCurrentOS() != OperatingSystem.WINDOWS && !outputFile.setExecutable(true)) {
            log.warn("failed to make file '{}' executable", outputFile.getCanonicalPath());
        }
    }

}
