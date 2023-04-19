package com.wangl.spring.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;

import java.util.Arrays;

/**
 * @ClassName ProjectSuggestionService
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/31 13:17
 */
public class ProjectSuggestionService {

    private final Project project;


    public ProjectSuggestionService(Project project) {
        this.project = project;
    }

    public void reIndex() {
        reIndex(Arrays.stream(ModuleManager.getInstance(project).getModules())
                .filter(module -> module.getName().contains("main"))
                .toArray(Module[]::new));
    }

    public void reIndex(Module[] modules) {
        for (Module module : modules) {
            if (module.getName().contains("main")){
                module.getService(SuggestionService.class).reIndex();
            }
        }
    }
}
