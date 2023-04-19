package com.wangl.spring.startup;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.util.messages.MessageBusConnection;
import com.wangl.spring.service.ProjectSuggestionService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenImportListener;


public class MavenReIndexingDependencyChangeSubscriberImpl implements StartupActivity {

    @Override
    public void runActivity(@NotNull Project project) {
        // This will trigger indexing
        ProjectSuggestionService service = project.getService(ProjectSuggestionService.class);

        try {
            MessageBusConnection connection = project.getMessageBus().connect();
            connection.subscribe(MavenImportListener.TOPIC, (MavenImportListener) (importedProjects, newModules) -> {
                boolean proceed = importedProjects.stream().anyMatch(
                        p -> project.getName().equals(p.getDisplayName()) && p.getDirectory().equals(project.getBasePath()));

                if (proceed) {
                    DumbService.getInstance(project).smartInvokeLater(() -> {
                        try {
                            Module[] modules = ModuleManager.getInstance(project).getModules();
                            if (modules.length > 0) {
                                service.reIndex(modules);
                            }
                        } catch (Throwable e) {
                        }
                    });
                } else {
                }
            });
        } catch (Throwable e) {
        }
    }
}
