package io.projectenv.shell;

import io.projectenv.core.commons.archive.ArchiveExtractorFactory;
import io.projectenv.core.commons.download.DownloadUrlSubstitutorFactory;
import io.projectenv.core.commons.download.ImmutableDownloadUrlDictionary;
import io.projectenv.core.commons.system.CPUArchitecture;
import io.projectenv.core.commons.system.OperatingSystem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

abstract class AbstractProjectEnvShellTest {

    private static final String PROJECT_ENV_CLI_VERSION = "3.8.0";

    private File tempDirectory;

    @BeforeEach
    public void setUpTempDirectory() throws IOException {
        tempDirectory = createTempDirectory();
    }

    @Test
    void executeProjectEnvShellWithFileOutput(@TempDir File projectRoot) throws Exception {
        var pathElement = setupProjectEnvCli();

        copyResourceToTarget("git-hook", new File(projectRoot, "hooks"));
        copyResourceToTarget("settings.xml", projectRoot);
        copyResourceToTarget("settings-user.xml", projectRoot);
        File configFile = copyResourceToTarget("project-env.toml", projectRoot);

        String outputTemplate = "zsh";
        File outputFile = new File(projectRoot, "project-env-output.sh");

        var output = executeProjectEnvShell(
                pathElement,
                "--config-file=" + configFile.getAbsolutePath(),
                "--output-template=" + outputTemplate,
                "--output-file=" + outputFile.getAbsolutePath(),
                "--project-root=" + projectRoot.getAbsolutePath(),
                "--debug"
        );

        assertThat(output).isEmpty();

        List<String> expectedContent = readAndPrepareExpectedOutput(projectRoot);
        List<String> actualContent = readAndPrepareActualOutput(outputFile);
        assertThat(actualContent).containsExactlyElementsOf(expectedContent);
    }

    @Test
    void executeProjectEnvShellWithStdOutput(@TempDir File projectRoot) throws Exception {
        var pathElement = setupProjectEnvCli();

        copyResourceToTarget("git-hook", new File(projectRoot, "hooks"));
        copyResourceToTarget("settings.xml", projectRoot);
        copyResourceToTarget("settings-user.xml", projectRoot);
        File configFile = copyResourceToTarget("project-env.toml", projectRoot);

        String outputTemplate = "zsh";

        var output = executeProjectEnvShell(
                pathElement,
                "--config-file=" + configFile.getAbsolutePath(),
                "--output-template=" + outputTemplate,
                "--project-root=" + projectRoot.getAbsolutePath(),
                "--debug"
        );

        List<String> expectedContent = readAndPrepareExpectedOutput(projectRoot);
        List<String> actualContent = readAndPrepareActualOutput(output);
        assertThat(actualContent).containsExactlyElementsOf(expectedContent);
    }

    @AfterEach
    public void tearDownTempDirectory() throws IOException {
        FileUtils.forceDelete(tempDirectory);
    }

    private File setupProjectEnvCli() throws IOException, URISyntaxException {
        var downloadUrl = getProjectEnvCliDownloadUrl();
        var tempArchiveFile = getTempArchiveFile(downloadUrl);
        downloadArchive(downloadUrl, tempArchiveFile);

        var tempArchiveExtractionDirectory = getTempArchiveExtractionDirectory(downloadUrl);
        extractArchive(tempArchiveFile, tempArchiveExtractionDirectory);

        return tempArchiveExtractionDirectory;
    }

    private String getProjectEnvCliDownloadUrl() {
        var dictionary = ImmutableDownloadUrlDictionary.builder()
                .putParameters("VERSION", PROJECT_ENV_CLI_VERSION)
                .putOperatingSystemSpecificParameters(
                        "OS",
                        Map.of(
                                OperatingSystem.MACOS, "macos",
                                OperatingSystem.LINUX, "linux",
                                OperatingSystem.WINDOWS, "windows"
                        )
                )
                .putOperatingSystemSpecificParameters(
                        "FILE_EXT",
                        Map.of(
                                OperatingSystem.MACOS, "tar.gz",
                                OperatingSystem.LINUX, "tar.gz",
                                OperatingSystem.WINDOWS, "zip"
                        )
                )
                .putCPUArchitectureSpecificParameters(
                        "CPU_ARCH",
                        Map.of(
                                CPUArchitecture.X64, "amd64"
                        )
                )
                .build();

        return DownloadUrlSubstitutorFactory
                .createDownloadUrlVariableSubstitutor(dictionary)
                .replace("https://github.com/Project-Env/project-env-core/releases/download/v${VERSION}/cli-${VERSION}-${OS}-${CPU_ARCH}.${FILE_EXT}");
    }

    private File getTempArchiveFile(String downloadUrl) throws IOException {
        var archiveFilename = FilenameUtils.getName(downloadUrl);

        return File.createTempFile("junit", archiveFilename, tempDirectory);
    }

    private File getTempArchiveExtractionDirectory(String downloadUrl) throws IOException {
        var archiveFilename = FilenameUtils.getName(downloadUrl);
        var archiveFileExtension = FilenameUtils.getExtension(downloadUrl);

        return createTempDirectory(archiveFilename.replace("." + archiveFileExtension, ""), tempDirectory);
    }

    private void downloadArchive(String downloadUrl, File target) throws IOException, URISyntaxException {
        try (var inputStream = new BufferedInputStream(new URI(downloadUrl).toURL().openStream());
             var outputStream = new FileOutputStream(target)) {

            IOUtils.copy(inputStream, outputStream);
        }
    }

    private void extractArchive(File archive, File target) throws IOException {
        ArchiveExtractorFactory.createArchiveExtractor().extractArchive(archive, target);
    }

    private File createTempDirectory() throws IOException {
        return createTempDirectory(null, null);
    }

    private File createTempDirectory(String suffix, File parent) throws IOException {
        var temporaryFolder = File.createTempFile("junit", suffix, parent);
        assertThat(temporaryFolder.delete()).isTrue();
        FileUtils.forceMkdir(temporaryFolder);

        return temporaryFolder;
    }

    protected abstract String executeProjectEnvShell(File pathElement, String... params) throws Exception;

    private File copyResourceToTarget(String resource, File target) throws Exception {
        File resultingFile = new File(target, resource);
        FileUtils.forceMkdirParent(resultingFile);

        try (InputStream inputStream = getClass().getResourceAsStream(resource);
             OutputStream outputStream = new FileOutputStream(resultingFile)) {

            IOUtils.copy(inputStream, outputStream);

            return resultingFile;
        }
    }

    private List<String> readAndPrepareExpectedOutput(File projectRoot) throws Exception {
        return IOUtils.readLines(getClass().getResourceAsStream("project-env-output.sh"), StandardCharsets.UTF_8)
                .stream()
                .filter(StringUtils::isNotBlank)
                .map(line -> line.replace("BASE_PATH", getCanonicalProjectPath(projectRoot)))
                .map(line -> line.replace("NODE_PLATFORM", getNodePlatformName()))
                .map(line -> line.replace("NODE_BIN_PATH", getNodeBinPath()))
                .map(line -> line.replace("JDK_HOME", getJdkHome()))
                .collect(Collectors.toList());
    }

    private List<String> readAndPrepareActualOutput(File outputFile) throws Exception {
        return readAndPrepareActualOutput(FileUtils.readFileToString(outputFile, StandardCharsets.UTF_8));
    }

    private List<String> readAndPrepareActualOutput(String output) throws Exception {
        return Arrays.stream(StringUtils.split(output, '\n'))
                .filter(StringUtils::isNotBlank)
                .map(StringUtils::trim)
                .map(line -> line.replaceAll("-[^/]{43}/", "-SHA256/"))
                .collect(Collectors.toList());
    }

    private String getCanonicalProjectPath(File projectRoot) {
        try {
            return projectRoot.getCanonicalPath()
                    .replaceAll(Pattern.quote("\\"), "/");
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid file", e);
        }
    }

    private String getNodePlatformName() {
        if (SystemUtils.IS_OS_MAC) {
            return "darwin";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return "win";
        } else {
            throw new IllegalStateException("unsupported os");
        }
    }

    private String getNodeBinPath() {
        if (SystemUtils.IS_OS_MAC) {
            return "/bin";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "/bin";
        } else if (SystemUtils.IS_OS_WINDOWS) {
            return "";
        } else {
            throw new IllegalStateException("unsupported os");
        }
    }

    private String getJdkHome() {
        if (SystemUtils.IS_OS_MAC) {
            return "/Contents/Home";
        }

        return "";
    }

}
