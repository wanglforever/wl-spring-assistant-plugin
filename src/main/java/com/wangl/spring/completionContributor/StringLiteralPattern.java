package com.wangl.spring.completionContributor;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @ClassName StringLiteralPattern
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/23 18:31
 */
public class StringLiteralPattern extends PatternCondition<PsiElement> {
    public StringLiteralPattern() {
        super("stringLiteralPattern()");
    }

    @Override
    public boolean accepts(@NotNull PsiElement psi, ProcessingContext context) {
        Language lang = PsiUtilCore.findLanguageFromElement(psi);
        ParserDefinition definition = LanguageParserDefinitions.INSTANCE.forLanguage(lang);
        if (definition == null) {
            return false;
        }

        ASTNode node = psi.getNode();
        if (node == null) {
            return false;
        }

        // support completions in string and comment literals
        TokenSet tokens = TokenSet.orSet(
                definition.getStringLiteralElements(),
                definition.getCommentTokens());
        if (tokens.contains(node.getElementType())) {
            return true;
        }

        return false;
    }
}
