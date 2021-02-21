package io.projectenv.shell.command;

import io.projectenv.core.configuration.ProjectEnvConfigurationFactory;
import io.projectenv.core.configuration.ToolsConfiguration;
import io.projectenv.core.tools.repository.ToolsRepository;
import io.projectenv.core.tools.repository.ToolsRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public abstract class AbstractProjectEnvCommand implements Callable<Integer> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Option(names = {"--project-root"}, defaultValue = ".")
    protected File projectRoot;

    @Option(names = {"--config-file"}, required = true)
    protected File configFile;

    @Override
    public Integer call() throws Exception {
        ToolsConfiguration toolsConfiguration = readToolsConfiguration();
        ToolsRepository toolsRepository = createToolsRepository(toolsConfiguration);

        executeCommand(toolsConfiguration, toolsRepository);

        return 0;
    }

    private ToolsConfiguration readToolsConfiguration() throws IOException {
        return ProjectEnvConfigurationFactory.createFromFile(configFile).getToolsConfiguration();
    }

    private ToolsRepository createToolsRepository(ToolsConfiguration toolsConfiguration) {
        File repositoryRoot = new File(projectRoot, toolsConfiguration.getToolsDirectory());

        return ToolsRepositoryFactory.createToolRepository(repositoryRoot);
    }

    protected abstract void executeCommand(ToolsConfiguration configuration, ToolsRepository repository) throws Exception;

}
