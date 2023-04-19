package com.wangl.spring.service.spi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @ClassName VirtualFileResolver
 * @Description TODO
 * @Author wangl
 * @Date 2023/4/18 17:08
 */
public interface VirtualFileResolver {

    void resolve(VirtualFile virtualFile, Project project);
}
