package com.wangl.spring.completionContributor;

import com.intellij.codeInsight.completion.*;
import com.intellij.lang.properties.parsing.PropertiesTokenTypes;
import com.intellij.patterns.PlatformPatterns;

/**
 * @ClassName PropertiesKeyCompletionContributor
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/23 18:28
 */
public class PropertiesKeyCompletionContributor extends CompletionContributor {
    public PropertiesKeyCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PropertiesTokenTypes.KEY_CHARACTERS),
                new PropertyCompletionProvider());
    }
}
