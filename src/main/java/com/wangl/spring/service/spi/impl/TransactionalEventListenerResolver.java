package com.wangl.spring.service.spi.impl;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.wangl.spring.service.spi.VirtualFileResolver;


import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wangl.spring.utils.Annotations.COMPONENT;
import static com.wangl.spring.utils.Annotations.TRANSACTIONAL_EVENT_LISTENER;

/**
 * @ClassName TransactionalEventListenerResolver
 * @Description TODO
 * @Author wangl
 * @Date 2023/4/18 17:11
 */
public class TransactionalEventListenerResolver {

    private static final Set<PsiMethod> methods = new HashSet<>();

    public static void resolve(VirtualFile file, Project project) {
        if (!file.getPath().contains("src/main/java")){
            return;
        }
        if (!file.isDirectory()){
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile instanceof PsiJavaFile){
                PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                PsiClass[] classes = psiJavaFile.getClasses();
                for (PsiClass psiClass : classes) {
                    processTransactionalEventListenerAnnotation(psiClass);
                }
            }
        }
    }

    private static void processTransactionalEventListenerAnnotation(PsiClass psiClass) {
        PsiAnnotation componentAnnotation = psiClass.getAnnotation(COMPONENT);
        if (componentAnnotation == null){
            return;
        }
        PsiMethod[] allMethods = psiClass.getAllMethods();
        for (PsiMethod method : allMethods) {
            PsiAnnotation transactionalEventListenerAnnotation = method.getAnnotation(TRANSACTIONAL_EVENT_LISTENER);
            if (transactionalEventListenerAnnotation != null){
                methods.add(method);
            }
        }
    }

    public static List<PsiMethod> getListener(String parameterType){
        return methods.stream()
                .filter(method -> {
                    PsiParameter[] parameters = method.getParameterList().getParameters();
                    if (parameters.length != 1){
                        return false;
                    }
                    return Objects.equals(parameters[0].getType().getCanonicalText(), parameterType);
                }).collect(Collectors.toList());
    }
}
