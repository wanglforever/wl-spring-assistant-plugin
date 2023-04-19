package com.wangl.spring.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.jvm.JvmParameter;
import com.intellij.lang.jvm.types.JvmType;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.wangl.spring.utils.Icons;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

import static com.wangl.spring.utils.Annotations.TRANSACTIONAL_EVENT_LISTENER;

/**
 * @ClassName TransactionalEventListenerLineMarkerProvider
 * @Description TODO
 * @Author wangl
 * @Date 2023/4/18 15:01
 */
public class TransactionalEventListenerLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiMethod)){
            return;
        }
        PsiMethod method = (PsiMethod) element;
        PsiAnnotation annotation = method.getAnnotation(TRANSACTIONAL_EVENT_LISTENER);
        if (annotation == null){
            return;
        }

        PsiParameter[] parameters = method.getParameterList().getParameters();
        if (parameters.length != 1){
            return;
        }
        PsiParameter parameter = parameters[0];
        String parameterType = parameter.getType().getCanonicalText();

        Project project = element.getProject();

        String className = "org.springframework.context.ApplicationEventPublisher";
        String methodName = "publishEvent";

        PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project));
        PsiMethod psiMethod = psiClass.findMethodsByName(methodName, true)[0];

        Collection<PsiReference> references = ReferencesSearch.search(psiMethod, GlobalSearchScope.projectScope(project)).findAll();
        PsiElement[] psiElements = references.stream()
                .filter(psiReference -> {
                    if (!(psiReference.getElement().getContext() instanceof PsiMethodCallExpression)){
                        return false;
                    }
                    PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) psiReference.getElement().getContext();
                    PsiExpressionList argumentList = methodCallExpression.getArgumentList();
                    PsiExpression[] expressions = argumentList.getExpressions();
                    if (expressions.length != 1){
                        return false;
                    }
                    return Objects.equals(expressions[0].getType().getCanonicalText(), parameterType);
                })
                .map(PsiReference::getElement).toArray(PsiElement[]::new);
        NavigationGutterIconBuilder navigationGutterIconBuilder = NavigationGutterIconBuilder.create(Icons.listener)
                .setAlignment(GutterIconRenderer.Alignment.CENTER)
                .setTooltipText(psiElements[0].getContext().getText())
                .setTargets(psiElements);
        RelatedItemLineMarkerInfo<PsiElement> lineMarkerInfo = navigationGutterIconBuilder.createLineMarkerInfo(element);
        result.add(lineMarkerInfo);
    }
}
