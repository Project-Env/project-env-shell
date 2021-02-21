package io.projectenv.shell.command;

import io.projectenv.core.configuration.ToolsConfiguration;
import io.projectenv.core.tools.repository.ToolsRepository;
import picocli.CommandLine.Command;

@Command(name = "clean")
public class ProjectEnvCleanCommand extends AbstractProjectEnvCommand {

    @Override
    protected void executeCommand(ToolsConfiguration configuration, ToolsRepository repository) throws Exception {
        repository.cleanAllToolsOfCurrentOSExcluding(configuration.getAllToolConfigurations());
    }

}
