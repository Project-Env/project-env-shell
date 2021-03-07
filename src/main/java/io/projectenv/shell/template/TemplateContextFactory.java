package io.projectenv.shell.template;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import io.projectenv.core.tools.info.MavenInfo;
import io.projectenv.core.tools.info.SimpleToolInfo;
import io.projectenv.core.tools.info.ToolInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class TemplateContextFactory {

    private static final TypeReference<List<Map<String, Object>>> TOOLS_CONTEXT_TYPE = new TypeReference<>() {
    };

    private TemplateContextFactory() {
        // noop
    }

    public static Map<String, Object> createContext(List<ToolInfo> toolInfo) {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new ToolInfoBeanSerializerModifier());
        module.addSerializer(new FileSerializer());
        module.addSerializer(new OptionalSerializer());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(module);

        return Map.of("tools", objectMapper.convertValue(toolInfo, TOOLS_CONTEXT_TYPE));
    }

    private static class ToolInfoBeanSerializerModifier extends BeanSerializerModifier {

        @SuppressWarnings("unchecked")
        @Override
        public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
            if (beanDesc.getType().isTypeOrSubTypeOf(ToolInfo.class)) {
                return new ToolInfoSerializer((JsonSerializer<ToolInfo>) serializer);
            }

            return super.modifySerializer(config, beanDesc, serializer);
        }

    }

    private static class ToolInfoSerializer extends JsonSerializer<ToolInfo> {

        private static final List<Class<? extends ToolInfo>> RELEVANT_IS_TOOL_PROPERTY_CLASSES = List.of(SimpleToolInfo.class, MavenInfo.class);

        private final JsonSerializer<ToolInfo> serializer;

        public ToolInfoSerializer(JsonSerializer<ToolInfo> jsonSerializer) {
            this.serializer = jsonSerializer;
        }

        @Override
        public void serialize(ToolInfo toolInfo, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            serializer.unwrappingSerializer(NameTransformer.NOP).serialize(toolInfo, jsonGenerator, serializerProvider);

            for (Class<? extends ToolInfo> clazz : RELEVANT_IS_TOOL_PROPERTY_CLASSES) {
                String propertyName = createIsToolPropertyName(clazz);

                jsonGenerator.writeBooleanField(propertyName, clazz.isAssignableFrom(toolInfo.getClass()));
            }

            jsonGenerator.writeEndObject();
        }

        private String createIsToolPropertyName(Class<? extends ToolInfo> clazz) {
            return "is" + clazz.getSimpleName().replace("Info", "");
        }

    }

    private static class FileSerializer extends StdSerializer<File> {

        protected FileSerializer() {
            super(File.class);
        }

        @Override
        public void serialize(File value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
            String canonicalPath = value.getCanonicalPath();

            // removes a trailing path separator if existing
            canonicalPath = canonicalPath.replaceAll(Pattern.quote(File.separator) + "$", "");

            jsonGenerator.writeString(canonicalPath);
        }

    }

    private static class OptionalSerializer extends StdSerializer<Optional<?>> {

        protected OptionalSerializer() {
            super(TypeFactory.defaultInstance().constructParametricType(Optional.class, WildcardType.class));
        }

        @Override
        public void serialize(Optional<?> value, JsonGenerator jsonGenerator, SerializerProvider provider) throws IOException {
            provider.defaultSerializeValue(value.orElse(null), jsonGenerator);
        }

    }

}
