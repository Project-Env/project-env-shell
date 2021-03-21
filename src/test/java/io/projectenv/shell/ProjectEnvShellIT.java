package io.projectenv.shell;

import io.projectenv.core.common.OperatingSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellIT {

    @Test
    void executeWithZshTemplate(@TempDir File projectRoot) throws Exception {
        copyResourceToTarget("git-hook", new File(projectRoot, "hooks"));
        File configFile = copyResourceToTarget("project-env.yml", projectRoot);

        executeAndAssertRefresh(projectRoot, configFile);
        executeAndAssertClean(projectRoot, configFile);
    }

    private void executeAndAssertRefresh(File projectRoot, File configFile) throws Exception {
        String outputTemplate = "zsh.peb";
        File outputFile = new File(projectRoot, "project-env-output.sh");

        Process process = new ProcessBuilder(
                "./target/project-env-shell",
                "refresh",
                "--config-file=" + configFile.getAbsolutePath(),
                "--output-template=" + outputTemplate,
                "--output-file=" + outputFile.getAbsolutePath(),
                "--project-root=" + projectRoot.getAbsolutePath()
        )
                .inheritIO()
                .start();

        boolean terminated = process.waitFor(5, TimeUnit.MINUTES);
        if (!terminated) {
            process.destroy();
        }
        assertThat(process.exitValue()).describedAs("process exit code").isZero();

        String expectedContent = readAndPrepareExpectedOutput(projectRoot);
        String actualContent = readAndPrepareActualOutput(outputFile);
        assertThat(actualContent).isEqualTo(expectedContent);
    }

    private void executeAndAssertClean(File projectRoot, File configFile) throws Exception {
        Process process = new ProcessBuilder(
                "./target/project-env-shell",
                "clean",
                "--config-file=" + configFile.getAbsolutePath(),
                "--project-root=" + projectRoot.getAbsolutePath()
        ).inheritIO().start();

        boolean terminated = process.waitFor(5, TimeUnit.MINUTES);
        if (!terminated) {
            process.destroy();
        }
        assertThat(process.exitValue()).describedAs("process exit code").isZero();
    }

    private File copyResourceToTarget(String resource, File target) throws Exception {
        File resultingFile = new File(target, resource);
        FileUtils.forceMkdirParent(resultingFile);

        try (InputStream inputStream = getClass().getResourceAsStream(resource);
             OutputStream outputStream = new FileOutputStream(resultingFile)) {
            IOUtils.copy(inputStream, outputStream);

            return resultingFile;
        }
    }

    private String readAndPrepareExpectedOutput(File projectRoot) throws Exception {
        return IOUtils.toString(getClass().getResourceAsStream("project-env-output.sh"), StandardCharsets.UTF_8)
                .replace("BASE_PATH", projectRoot.getCanonicalPath())
                .replace("NODE_PLATFORM", getNodePlatformName())
                .replace("NODE_BIN_PATH", getNodeBinPath())
                .replace("JDK_HOME", getJdkHome())
                .replace("/", File.separator);
    }

    private String readAndPrepareActualOutput(File outputFile) throws Exception {
        return FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8)
                .replaceAll(".{8}-.{4}-.{4}-.{4}-.{13}", "")
                .replace("/", File.separator);
    }

    private String getNodePlatformName() {
        switch (OperatingSystem.getCurrentOS()) {
            case MACOS:
                return "darwin";
            case LINUX:
                return "linux";
            case WINDOWS:
                return "win";
            default:
                throw new IllegalStateException("unsupported os " + OperatingSystem.getCurrentOS());
        }
    }

    private String getNodeBinPath() {
        switch (OperatingSystem.getCurrentOS()) {
            case MACOS:
            case LINUX:
                return "/bin";
            case WINDOWS:
                return "";
            default:
                throw new IllegalStateException("unsupported os " + OperatingSystem.getCurrentOS());
        }
    }

    private String getJdkHome() {
        if (OperatingSystem.getCurrentOS() == OperatingSystem.MACOS) {
            return "/Contents/Home";
        }

        return "";
    }

}
