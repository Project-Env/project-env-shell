package io.projectenv.shell.template;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TemplateProcessorTest {

    @Test
    void testCustomTemplate(@TempDir File tempDir) throws IOException {
        var expectedContent = "custom-template";

        var customTemplate = createCustomTemplate(expectedContent, tempDir);
        var actualContent = TemplateProcessor.processTemplate(customTemplate.getAbsolutePath(), Map.of());

        assertThat(actualContent).isEqualTo(expectedContent);
    }

    private File createCustomTemplate(String content, File parentDirectory) throws IOException {
        var customTemplate = File.createTempFile("custom-pebble", ".peb", parentDirectory);
        FileUtils.write(customTemplate, content, StandardCharsets.UTF_8);

        return customTemplate;
    }

}