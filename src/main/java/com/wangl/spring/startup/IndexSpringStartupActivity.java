package com.wangl.spring.startup;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.wangl.spring.service.ProjectSuggestionService;
import org.jetbrains.annotations.NotNull;

/**
 * @ClassName IndexSpringStartupActivity
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/29 15:26
 */
public class IndexSpringStartupActivity implements StartupActivity.Background {
    @Override
    public void runActivity(@NotNull Project project) {
        ProgressManager.getInstance().run(
                new Task.Backgroundable(project, "Index @ConfigurationProperties") {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        DumbService.getInstance(project).runReadActionInSmartMode(() -> {
                            project.getService(ProjectSuggestionService.class).reIndex();
                        });
                    }
                }
        );
    }
}
