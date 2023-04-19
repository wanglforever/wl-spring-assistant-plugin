package com.wangl.spring.service;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.OrderEnumerator;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.psi.*;
import com.wangl.spring.service.spi.VirtualFileResolver;
import com.wangl.spring.service.spi.impl.TransactionalEventListenerResolver;
import com.wangl.spring.suggestion.SuggestionKeyNode;
import com.wangl.spring.suggestion.SuggestionNodeTree;
import com.wangl.spring.utils.SpringBootPropertiesResolver;
import org.apache.commons.lang.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static com.intellij.openapi.application.ApplicationManager.getApplication;

/**
 * @ClassName SuggestionService
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/31 10:49
 */
public class SuggestionService {

    private final Module module;
    private final SuggestionNodeTree suggestionNodeTree;
    private final Set<String> indexed;
    private boolean indexAvailable = false;

    public SuggestionService(Module module) {
        this.module = module;
        this.suggestionNodeTree = new SuggestionNodeTree();
        this.indexed = new HashSet<>();
    }

    public void reIndex(){
        getApplication().executeOnPooledThread(() -> {
            DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> {
                synchronized (SuggestionService.this){
                    StopWatch moduleTimer = new StopWatch();
                    moduleTimer.start();
                    try {
                        indexAvailable = false;
                        OrderEnumerator moduleOrderEnumerator = OrderEnumerator.orderEntries(module);
                        rebuildSuggestionNodeTree(moduleOrderEnumerator);
                        resolveSources(moduleOrderEnumerator);
                        indexAvailable = true;
                    } finally {
                        moduleTimer.stop();
                        System.out.println("module：" + module.getName() + ", index " + moduleTimer);
                    }
                }
            });
        });
    }

    private void resolveSources(OrderEnumerator moduleOrderEnumerator) {
        for (VirtualFile virtualFile : moduleOrderEnumerator.sources().getRoots()) {
            VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    TransactionalEventListenerResolver.resolve(file, module.getProject());
                    return true;
                }
            });
        }
    }

    private void rebuildSuggestionNodeTree(OrderEnumerator moduleOrderEnumerator) {
        for (VirtualFile virtualFile : moduleOrderEnumerator.recursively().classes().getRoots()) {
            if (indexed.contains(virtualFile.getPath()) ||
                    virtualFile.getPath().contains("/jre/")){
                continue;
            }

            VfsUtilCore.visitChildrenRecursively(virtualFile, new VirtualFileVisitor() {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (!file.isDirectory()){
                        PsiFile psiFile = PsiManager.getInstance(module.getProject()).findFile(file);
                        if (psiFile instanceof PsiJavaFile){
                            PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                            PsiClass[] classes = psiJavaFile.getClasses();
                            for (PsiClass psiClass : classes) {
                                processConfigurationPropertiesAnnotation(psiClass);
                            }
                        }
                    }
                    return true;
                }
            });

            indexed.add(virtualFile.getPath());
        }
    }

    private void processConfigurationPropertiesAnnotation(PsiClass psiClass){
        if (!SpringBootPropertiesResolver.isConfiguration(psiClass) &&
                !SpringBootPropertiesResolver.isAnnotatedConfigurationProperties(psiClass)){
            return;
        }
        if (SpringBootPropertiesResolver.isConfiguration(psiClass)){
            SpringBootPropertiesResolver.analysisConfigurationClass(psiClass, suggestionNodeTree);
        }
        if (SpringBootPropertiesResolver.isAnnotatedConfigurationProperties(psiClass)){
            SpringBootPropertiesResolver.analysisConfigurationPropertiesClass(psiClass, suggestionNodeTree);
        }
    }

    public List<SuggestionKeyNode> suggestByPrefix(String prefix){
        if (!prefix.contains(".")){
            return suggestionNodeTree.getRootNodes().stream()
                    .filter(node -> node.getText().contains(prefix))
                    .collect(Collectors.toList());
        }
        int index = prefix.lastIndexOf(".");
        SuggestionKeyNode suggestionKeyNode = suggestionNodeTree.searchNode(prefix.substring(0, index));
        if (suggestionKeyNode == null){
            return Collections.emptyList();
        }
        if (index == prefix.length() - 1){
            return suggestionKeyNode.getChildren();
        }
        return suggestionKeyNode.getChildren().stream()
                .filter(node -> node.getText().startsWith(prefix.substring(index + 1)))
                .collect(Collectors.toList());
    }

    public List<SuggestionKeyNode> suggestByPrefix(List<String> parents, String prefix){
        if (CollectionUtils.isEmpty(parents)){
            return suggestByPrefix(prefix);
        }
        String parentPrefix = String.join(".", parents);
        SuggestionKeyNode parentNode = suggestionNodeTree.searchNode(parentPrefix);
        if (parentNode == null){
            return Collections.emptyList();
        }
        return parentNode.getChildren().stream()
                .filter(node -> node.getText().startsWith(prefix))
                .collect(Collectors.toList());
    }

    public PsiElement searchByPropertyName(String propertyName){
        SuggestionKeyNode suggestionKeyNode = suggestionNodeTree.searchNode(propertyName);
        if (suggestionKeyNode != null){
            return suggestionKeyNode.getPsiElement();
        }
        return null;
    }
}
