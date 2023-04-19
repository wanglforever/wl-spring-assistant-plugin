package com.wangl.spring.declarationHandler;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandlerBase;
import com.intellij.lang.properties.psi.impl.PropertyKeyImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.wangl.spring.service.SuggestionService;
import com.wangl.spring.utils.PsiCustomUtil;
import com.wangl.spring.utils.StringPropertiesUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLScalarText;
import org.jetbrains.yaml.psi.YAMLSequence;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.wangl.spring.utils.PsiCustomUtil.findModule;
import static com.wangl.spring.utils.PsiCustomUtil.truncateIdeaDummyIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * @ClassName YamlKeyGotoDeclarationHandler
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/27 15:01
 */
public class YamlKeyGotoDeclarationHandler extends GotoDeclarationHandlerBase {
    @Override
    public @Nullable PsiElement getGotoDeclarationTarget(@Nullable PsiElement source, Editor editor) {
        if (!(source instanceof LeafPsiElement)) return null;
        Module module = findModule(source);
        if (module == null) {
            return null;
        }
        SuggestionService service = module.getService(SuggestionService.class);
        PsiElement elementContext = source.getContext();
        PsiElement parent = requireNonNull(elementContext).getParent();
        if (parent instanceof YAMLSequence) {
            return null;
        }

        List<String> ancestralKeys = new ArrayList<>();
        PsiElement context = elementContext;
        do {
            if (context instanceof YAMLKeyValue) {
                ancestralKeys.add(0, truncateIdeaDummyIdentifier(((YAMLKeyValue) context).getKeyText()));
            }
            context = requireNonNull(context).getParent();
        } while (context != null);

        String propertyName = "";
        if (!CollectionUtils.isEmpty(ancestralKeys)){
            propertyName += String.join(".", ancestralKeys);
        }
        return service.searchByPropertyName(propertyName);
    }
}
