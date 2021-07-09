package io.projectenv.shell.nativeimage;

import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.oracle.svm.core.annotate.AutomaticFeature;
import io.projectenv.core.cli.api.ToolInfo;
import io.projectenv.core.commons.nativeimage.NativeImageHelper;
import io.projectenv.core.commons.process.ProcessOutputWriterAccessor;
import io.projectenv.shell.template.TemplateProcessor;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

import java.io.IOException;
import java.util.Map;

import static io.projectenv.core.commons.nativeimage.NativeImageHelper.registerClassForReflection;
import static io.projectenv.core.commons.nativeimage.NativeImageHelper.registerResource;


@AutomaticFeature
public class ProjectEnvFeature implements Feature {

    private static final String BASE_PACKAGE = "io.projectenv";

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        configureSlf4j();
        configureProcessOutputWriter();
        registerToolInfo();
        registerGsonSupport();
        registerTemplates();
    }

    private void configureSlf4j() {
        RuntimeClassInitialization.initializeAtBuildTime("org.slf4j");
    }

    private void configureProcessOutputWriter() {
        RuntimeClassInitialization.initializeAtBuildTime(ProcessOutputWriterAccessor.class);
    }

    private void registerToolInfo() {
        NativeImageHelper.registerClassAndSubclassesForReflection(ToolInfo.class);
    }

    private void registerGsonSupport() {
        try {
            NativeImageHelper.registerService(TypeAdapterFactory.class);
            NativeImageHelper.registerFieldsWithAnnotationForReflection(BASE_PACKAGE, SerializedName.class);
        } catch (IOException e) {
            throw new IllegalStateException("failed to register services for usage in native-image");
        }
    }

    private void registerTemplates() {
        try {
            // Since it is possible to provide its own Pebble template,
            // we don't want to restrict the available String methods.
            registerClassForReflection(String.class);

            // register map type since it is the main model object
            registerClassForReflection(Map.class);

            registerTemplate("github-actions.peb");
            registerTemplate("zsh.peb");
            registerTemplate("cygwin.peb");
        } catch (IOException e) {
            throw new IllegalStateException("failed to register templates for usage in native-image");
        }
    }

    private void registerTemplate(String templateName) throws IOException {
        registerResource(resolveTemplatePath(templateName));
    }

    private String resolveTemplatePath(String templateName) {
        return getTemplatesBasePath() + templateName;
    }

    private String getTemplatesBasePath() {
        return TemplateProcessor.class.getPackageName().replace(".", "/") + "/";
    }

}
