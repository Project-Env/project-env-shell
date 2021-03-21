package io.projectenv.shell;

import io.projectenv.shell.command.ProjectEnvCleanCommand;
import io.projectenv.shell.command.ProjectEnvRefreshCommand;
import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "project-env-shell", subcommands = {
        ProjectEnvRefreshCommand.class,
        ProjectEnvCleanCommand.class,
})
public final class ProjectEnvShell {

    public static void main(String[] args) {
        System.exit(new CommandLine(new ProjectEnvShell()).execute(args));
    }

}
