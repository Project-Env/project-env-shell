package io.projectenv.shell.nativeimage;

import com.oracle.svm.core.jdk.Resources;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;

public final class NativeImageHelper {

    private static final String SERVICE_REGISTRATION_FILE_PATH = "META-INF/services/";

    private static final Logger LOG = LoggerFactory.getLogger(NativeImageHelper.class);

    private NativeImageHelper() {
        // noop
    }

    public static void registerResource(String resource) throws IOException {
        try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(resource)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("resource " + resource + " could not be resolved");
            }

            LOG.info("registering resource '{}' in native image", resource);
            Resources.registerResource(resource, inputStream);
        }
    }

    public static void registerService(Class<?> clazz) throws IOException {
        registerClassAndSubclassesForReflection(clazz);
        registerResource(SERVICE_REGISTRATION_FILE_PATH + clazz.getName());
    }

    public static void registerClassAndSubclassesForReflection(Class<?> clazz) {
        Reflections reflections = new Reflections(clazz.getPackageName());
        for (Class<?> subClazz : reflections.getSubTypesOf(clazz)) {
            registerClassForReflection(subClazz);
        }
    }

    public static void registerClassForReflection(Class<?> clazz) {
        LOG.info("registering class '{}' for reflection in native image", clazz.getName());
        RuntimeReflection.register(clazz);
        RuntimeReflection.register(clazz.getDeclaredMethods());
        RuntimeReflection.register(clazz.getDeclaredFields());

        if (isConcreteClass(clazz)) {
            RuntimeReflection.register(clazz.getDeclaredConstructors());
        }

        for (Class<?> innerClazz : clazz.getDeclaredClasses()) {
            registerClassForReflection(innerClazz);
        }
    }

    private static boolean isConcreteClass(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

}
