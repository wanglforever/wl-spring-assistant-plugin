package com.wangl.spring.completionContributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.wangl.spring.service.SuggestionService;
import com.wangl.spring.suggestion.SuggestionKeyNode;
import com.wangl.spring.utils.Icons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static com.wangl.spring.utils.PsiCustomUtil.findModule;
import static com.wangl.spring.utils.PsiCustomUtil.truncateIdeaDummyIdentifier;
import static java.util.Objects.requireNonNull;

/**
 * @ClassName YamlCompletionProvider
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/28 16:31
 */
public class YamlCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext processingContext,
                                  @NotNull CompletionResultSet result) {

        PsiElement position = parameters.getPosition();
        if (position instanceof PsiComment) {
            return;
        }

        Module module = findModule(position);
        if (module == null) {
            return;
        }
        SuggestionService service = module.getService(SuggestionService.class);

        PsiElement elementContext = position.getContext();
        PsiElement parent = requireNonNull(elementContext).getParent();
        if (parent instanceof YAMLSequence) {
            return;
        }

        String currentPrefix = truncateIdeaDummyIdentifier(position);

        List<String> ancestralKeys = new ArrayList<>();
        PsiElement context = elementContext;
        do {
            if (context instanceof YAMLKeyValue) {
                ancestralKeys.add(0, truncateIdeaDummyIdentifier(((YAMLKeyValue) context).getKeyText()));
            }
            context = requireNonNull(context).getParent();
        } while (context != null);

        List<SuggestionKeyNode> suggestionKeyNodes = service.suggestByPrefix(ancestralKeys, currentPrefix);

        for (SuggestionKeyNode node : suggestionKeyNodes) {
            LookupElement element = LookupElementBuilder.create(node.getText())
                    .withIcon(Icons.leaf)
                    .withCaseSensitivity(false);
            result.addElement(element);
        }
    }
}
