package io.projectenv.shell;

import io.projectenv.core.common.OperatingSystem;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectEnvShellTest {

    @Test
    void executeWithZshTemplate(@TempDir File projectRoot) throws Exception {
        File configFile = copyResourceToTarget("project-env.yml", projectRoot);
        String outputTemplate = "zsh.peb";
        File outputFile = new File(projectRoot, "project-env-output.sh");

        ProjectEnvShell.main(new String[]{
                "--config-file=" + configFile.getAbsolutePath(),
                "--output-template=" + outputTemplate,
                "--output-file=" + outputFile.getAbsolutePath(),
                "--project-root=" + projectRoot.getAbsolutePath()
        });

        String expectedContent = readAndPrepareExpectedOutput(projectRoot);
        assertThat(outputFile).hasContent(expectedContent);
    }

    private File copyResourceToTarget(String resource, File target) throws Exception {
        File resultingFile = new File(target, resource);

        try (InputStream inputStream = getClass().getResourceAsStream(resource);
             OutputStream outputStream = new FileOutputStream(resultingFile)) {
            IOUtils.copy(inputStream, outputStream);

            return resultingFile;
        }
    }

    private String readAndPrepareExpectedOutput(File projectRoot) throws Exception {
        return IOUtils.toString(getClass().getResourceAsStream("project-env-output.sh"), StandardCharsets.UTF_8)
                .replace("BASE_PATH", projectRoot.getCanonicalPath())
                .replace("OPERATING_SYSTEM", OperatingSystem.getCurrentOS().name().toLowerCase(Locale.ROOT))
                .replace("NODE_PLATFORM", getNodePlatformName())
                .replace("JDK_HOME", getJdkHome());
    }

    private String getNodePlatformName() {
        switch (OperatingSystem.getCurrentOS()) {
            case MACOS:
                return "darwin";
            case LINUX:
                return "linux";
            default:
                throw new IllegalStateException("unsupported os " + OperatingSystem.getCurrentOS());
        }
    }

    private String getJdkHome() {
        if(OperatingSystem.getCurrentOS() == OperatingSystem.MACOS) {
            return "/Contents/Home";
        }

        return "";
    }

}
