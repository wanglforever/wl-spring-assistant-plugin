package com.wangl.spring.completionContributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.wangl.spring.service.SuggestionService;
import com.wangl.spring.suggestion.SuggestionKeyNode;
import com.wangl.spring.utils.Icons;
import com.wangl.spring.utils.PsiCustomUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @ClassName PropertyCompletionProvider
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/24 14:10
 */
public class PropertyCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters,
                                  @NotNull ProcessingContext context,
                                  @NotNull CompletionResultSet result) {

        // 获取用户输入的前缀
        PsiElement position = parameters.getPosition();
        int length = CompletionUtilCore.DUMMY_IDENTIFIER.length();
        String text = position.getText();
        String prefix = text.substring(0,text.length() - length + 1);
        // 如果前缀为空白字符，则不进行任何操作
        if (prefix.trim().isEmpty()) {
            return;
        }
        // 自定义前缀匹配器
        result = result.withPrefixMatcher(new CustomPrefixMatcher(prefix));
        // 获取所有建议
        SuggestionService service = PsiCustomUtil.findModule(position).getService(SuggestionService.class);
        List<SuggestionKeyNode> suggestionKeyNodes = service.suggestByPrefix(prefix);

        // 添加匹配结果
        for (SuggestionKeyNode node : suggestionKeyNodes) {
            LookupElement element = LookupElementBuilder.create(SuggestionKeyNode.getSuggestion(node))
                    .withIcon(Icons.leaf)
                    .withCaseSensitivity(false);
            result.addElement(element);
        }
    }

    public static class CustomPrefixMatcher extends PrefixMatcher {
        public CustomPrefixMatcher(String s) {
            super(s);
        }

        @Override
        public boolean prefixMatches(@NotNull String name) {
            if (myPrefix.isEmpty() || myPrefix.endsWith(".")) {
                return true;
            }
            return name.contains(myPrefix);
        }

        @Override
        public @NotNull PrefixMatcher cloneWithPrefix(@NotNull String prefix) {
            return new CustomPrefixMatcher(prefix);
        }
    }
}
