package io.projectenv.shell;

import com.google.gson.reflect.TypeToken;
import io.projectenv.commons.gson.GsonFactory;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public final class ToolInfoParser {

    private static final Type TOOL_INFOS_TYPE = new TypeToken<Map<String, List<ToolInfo>>>() {
    }.getType();

    private ToolInfoParser() {
        // noop
    }

    public static Map<String, List<ToolInfo>> fromJson(String rawToolInfos) {
        return GsonFactory.createGson().fromJson(rawToolInfos, TOOL_INFOS_TYPE);
    }

}
