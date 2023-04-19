package com.wangl.spring.completionContributor;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;

/**
 * @ClassName YamlKeyCompletionContributor
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/28 16:30
 */
public class YamlKeyCompletionContributor extends CompletionContributor {
    public YamlKeyCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement().withLanguage(YAMLLanguage.INSTANCE),
                new YamlCompletionProvider());
    }
}
