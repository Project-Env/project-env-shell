package ch.projectenv.shell.template;

import ch.projectenv.core.toolinfo.MavenInfo;
import ch.projectenv.core.toolinfo.ToolInfo;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Test;
import com.mitchellbosecke.pebble.template.EvaluationContext;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import org.apache.commons.lang3.ClassPathUtils;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateProcessor {

    private final PebbleEngine pebbleEngine = new PebbleEngine
            .Builder()
            .extension(new ProjectEnvShellPebbleExtension())
            .strictVariables(true)
            .build();

    public String processTemplate(String template, List<ToolInfo> toolInfos) throws Exception {
        PebbleTemplate compiledTemplate = pebbleEngine.getTemplate(resolveTemplate(template));

        Map<String, Object> context = new HashMap<>();
        context.put("tools", toolInfos);

        Writer writer = new StringWriter();
        compiledTemplate.evaluate(writer, context);

        return writer.toString();
    }

    private String resolveTemplate(String template) {
        File templateFile = new File(template);
        if (templateFile.exists()) {
            return template;
        }

        return ClassPathUtils.toFullyQualifiedPath(getClass(), template);
    }

    private static class IsMavenTest implements Test {

        @Override
        public boolean apply(Object input, Map<String, Object> args, PebbleTemplate self, EvaluationContext context, int lineNumber) throws PebbleException {
            if (input == null) {
                throw new PebbleException(null, "Can not pass null value to \"maven\" test.", lineNumber, self.getName());
            }

            return MavenInfo.class.isAssignableFrom(input.getClass());
        }

        @Override
        public List<String> getArgumentNames() {
            return null;
        }

    }

    private static class ProjectEnvShellPebbleExtension extends AbstractExtension {

        @Override
        public Map<String, Test> getTests() {
            return Map.of("maven", new IsMavenTest());
        }

    }

}
