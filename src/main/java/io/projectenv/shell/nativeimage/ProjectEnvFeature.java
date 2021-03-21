package io.projectenv.shell.nativeimage;

import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.oracle.svm.core.annotate.AutomaticFeature;
import io.projectenv.core.configuration.*;
import io.projectenv.core.tools.info.ToolInfo;
import io.projectenv.core.tools.repository.impl.catalogue.ToolCatalogue;
import io.projectenv.core.tools.repository.impl.catalogue.ToolEntry;
import io.projectenv.core.tools.service.collector.ToolInfoCollector;
import io.projectenv.core.tools.service.installer.ToolInstaller;
import io.projectenv.core.tools.service.resources.LocalToolResourcesProcessor;
import io.projectenv.shell.template.TemplateProcessor;
import org.apache.commons.compress.archivers.zip.ZipExtraField;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeClassInitialization;

import java.io.IOException;
import java.util.Map;

import static io.projectenv.shell.nativeimage.NativeImageHelper.*;

@AutomaticFeature
public class ProjectEnvFeature implements Feature {

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        configureSlf4j();
        registerZipExtraFieldClasses();
        registerConfigurationClasses();
        registerInfoClasses();
        registerCatalogueClasses();
        registerServices();
        registerTemplates();
    }

    private void configureSlf4j() {
        RuntimeClassInitialization.initializeAtBuildTime("org.slf4j");
    }

    private void registerZipExtraFieldClasses() {
        // Since Apache Commons Compress uses reflection to register the ZipExtraField
        // implementations, we have to register them for Reflection support.
        NativeImageHelper.registerClassAndSubclassesForReflection(ZipExtraField.class);
    }

    private void registerConfigurationClasses() {
        registerClassAndSubclassesForReflection(DownloadUri.class);
        registerClassAndSubclassesForReflection(PostExtractionCommand.class);
        registerClassAndSubclassesForReflection(ProjectEnvConfiguration.class);
        registerClassAndSubclassesForReflection(ToolConfiguration.class);
        registerClassAndSubclassesForReflection(ToolsConfiguration.class);
    }

    private void registerInfoClasses() {
        registerClassAndSubclassesForReflection(ToolInfo.class);
    }

    private void registerCatalogueClasses() {
        registerClassAndSubclassesForReflection(ToolCatalogue.class);
        registerClassAndSubclassesForReflection(ToolEntry.class);
        registerClassAndSubclassesForReflection(StdSerializer.class);
    }

    private void registerServices() {
        try {
            registerService(ToolInfoCollector.class);
            registerService(ToolInstaller.class);
            registerService(LocalToolResourcesProcessor.class);
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
