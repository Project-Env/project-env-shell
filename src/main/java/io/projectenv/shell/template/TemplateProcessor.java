package io.projectenv.shell.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.projectenv.core.toolinfo.ToolInfo;
import org.apache.commons.lang3.ClassPathUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

public final class TemplateProcessor {

    private static final PebbleEngine PEBBLE_ENGINE = new PebbleEngine
            .Builder()
            .strictVariables(true)
            .build();

    private TemplateProcessor() {
        // noop
    }

    public static String processTemplate(String template, List<ToolInfo> toolInfos) throws IOException {
        PebbleTemplate compiledTemplate = PEBBLE_ENGINE.getTemplate(resolveTemplate(template));

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, TemplateContextFactory.createContext(toolInfos));

        return writer.toString();
    }

    private static String resolveTemplate(String template) {
        File templateFile = new File(template);
        if (templateFile.exists()) {
            return template;
        }

        return ClassPathUtils.toFullyQualifiedPath(TemplateProcessor.class, template);
    }

}
