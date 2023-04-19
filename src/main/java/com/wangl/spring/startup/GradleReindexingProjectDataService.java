package com.wangl.spring.startup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.ProjectKeys;
import com.intellij.openapi.externalSystem.model.project.ModuleData;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.externalSystem.util.Order;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.wangl.spring.service.ProjectSuggestionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil.getExternalRootProjectPath;
import static java.util.Arrays.stream;

/**
 * Callback that gets invoked by gradle as soon as the project is imported successfully
 */
@Order(5000)
public class GradleReindexingProjectDataService
        extends AbstractProjectDataService<ModuleData, Void> {

    @NotNull
    @Override
    public Key<ModuleData> getTargetDataKey() {
        return ProjectKeys.MODULE;
    }

    @Override
    public void onSuccessImport(@NotNull Collection<DataNode<ModuleData>> imported,
                                @Nullable ProjectData projectData, @NotNull Project project,
                                @NotNull IdeModelsProvider modelsProvider) {
        if (projectData != null) {
            DumbService.getInstance(project).smartInvokeLater(() -> {
                ProjectSuggestionService service = project.getService(ProjectSuggestionService.class);

                try {
                    Module[] validModules = stream(modelsProvider.getModules()).filter(module -> {
                        String externalRootProjectPath = getExternalRootProjectPath(module);
                        return externalRootProjectPath != null && externalRootProjectPath
                                .equals(projectData.getLinkedExternalProjectPath());
                    }).toArray(Module[]::new);

                    if (validModules.length > 0) {
                        service.reIndex(validModules);
                    }
                } catch (Throwable e) {
                }
            });
        }
    }
}
