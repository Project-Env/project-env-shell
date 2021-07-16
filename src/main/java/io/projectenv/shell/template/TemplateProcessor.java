package io.projectenv.shell.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Filter;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import io.projectenv.core.cli.api.ToolInfo;
import org.apache.commons.lang3.ClassPathUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.regex.Pattern;

public final class TemplateProcessor {

    public static final String PEBBLE_TEMPLATE_EXT = ".peb";

    private static final PebbleEngine PEBBLE_ENGINE = new PebbleEngine
            .Builder()
            .strictVariables(false)
            .extension(new PebbleExtension())
            .build();

    private TemplateProcessor() {
        // noop
    }

    public static String processTemplate(String template, Map<String, List<ToolInfo>> toolInfos) throws IOException {
        PebbleTemplate compiledTemplate = PEBBLE_ENGINE.getTemplate(resolveTemplate(template));

        Writer writer = new StringWriter();

        var context = new HashMap<String, Object>();
        context.put("toolInfos", toolInfos);

        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }

    private static String resolveTemplate(String template) {
        var templateFile = new File(template);
        if (templateFile.exists()) {
            return template;
        }

        if (StringUtils.endsWith(template, PEBBLE_TEMPLATE_EXT)) {
            return ClassPathUtils.toFullyQualifiedPath(TemplateProcessor.class, template);
        }

        return ClassPathUtils.toFullyQualifiedPath(TemplateProcessor.class, template + PEBBLE_TEMPLATE_EXT);
    }

    private static class PebbleExtension extends AbstractExtension {

        @Override
        public Map<String, Filter> getFilters() {
            return Map.of("path", new PathFilter());
        }

    }

    private static class PathFilter implements Filter {

        @Override
        public List<String> getArgumentNames() {
            return Collections.emptyList();
        }

        @Override
        public Object apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) {
            try {
                String canonicalPath = ((File) input).getCanonicalPath();

                // removes a trailing path separator if existing
                canonicalPath = canonicalPath.replaceAll(Pattern.quote(File.separator) + "$", "");

                return canonicalPath;
            } catch (IOException e) {
                throw new IllegalArgumentException("invalid file", e);
            }
        }

    }

}
