package com.wangl.spring.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.wangl.spring.service.spi.impl.TransactionalEventListenerResolver;
import com.wangl.spring.utils.Icons;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.wangl.spring.utils.Annotations.TRANSACTIONAL_EVENT_LISTENER;

/**
 * @ClassName ApplicationEventPublisherLineMarkerProvider
 * @Description TODO
 * @Author wangl
 * @Date 2023/4/18 15:01
 */
public class ApplicationEventPublisherLineMarkerProvider extends RelatedItemLineMarkerProvider {
    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo<?>> result) {
        if (!(element instanceof PsiMethodCallExpression)){
            return;
        }
        PsiMethodCallExpression psiMethodCallExpression = (PsiMethodCallExpression) element;
        String methodName = psiMethodCallExpression.getMethodExpression().getReferenceName();
        PsiExpression qualifierExpression = psiMethodCallExpression.getMethodExpression().getQualifierExpression();
        if (qualifierExpression == null){
            return;
        }
        PsiType expressionType = qualifierExpression.getType();
        if (expressionType == null){
            return;
        }
        String className = expressionType.getCanonicalText();
        if (Objects.equals(className, "org.springframework.context.ApplicationEventPublisher") &&
                Objects.equals(methodName, "publishEvent")){
            PsiExpressionList argumentList = psiMethodCallExpression.getArgumentList();
            PsiExpression[] expressions = argumentList.getExpressions();
            if (expressions.length != 1){
                return;
            }
            List<PsiMethod> listenerMethods = TransactionalEventListenerResolver.getListener(expressions[0].getType().getCanonicalText());
            if (CollectionUtils.isEmpty(listenerMethods)){
                return;
            }
            NavigationGutterIconBuilder navigationGutterIconBuilder = NavigationGutterIconBuilder.create(Icons.publisher)
                    .setAlignment(GutterIconRenderer.Alignment.CENTER)
                    .setTooltipText(listenerMethods.get(0).getContext().getText())
                    .setTargets(listenerMethods);
            RelatedItemLineMarkerInfo<PsiElement> lineMarkerInfo = navigationGutterIconBuilder.createLineMarkerInfo(element);
            result.add(lineMarkerInfo);
        }
    }
}
