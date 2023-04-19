package com.wangl.spring.declarationHandler;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.wangl.spring.service.SuggestionService;
import com.wangl.spring.utils.PsiCustomUtil;
import com.wangl.spring.utils.StringPropertiesUtil;
import org.jetbrains.annotations.Nullable;

/**
 * @ClassName PropertiesKeyGotoDeclarationHandler
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/27 15:01
 */
public class PropertiesKeyGotoDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement source, Editor editor) {
        if (!(source instanceof PropertyKeyImpl)) return null;
        String propertyKey = StringPropertiesUtil.toCamelCase(source.getText().replaceAll("\\[\\d*\\]", ""), "-");
        Module module = PsiCustomUtil.findModule(source);
        SuggestionService service = module.getService(SuggestionService.class);
        return service.searchByPropertyName(propertyKey);
    }
}
