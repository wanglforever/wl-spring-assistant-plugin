package com.wangl.spring.suggestion;

import com.intellij.psi.PsiElement;
import lombok.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @ClassName SuggestionKeyNode
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/29 17:32
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "text")
public class SuggestionKeyNode {

    private String text;
    private SuggestionKeyNode parent;
    private List<SuggestionKeyNode> children;
    private SuggestionNodeType type;
    private PsiElement psiElement;
    private int layerLevel;

    public static SuggestionKeyNode newInstance(String text, SuggestionKeyNode parent){
        return SuggestionKeyNode.builder().text(text).parent(parent).children(new ArrayList<>()).build();
    }

    public void addChild(SuggestionKeyNode child){
        this.getChildren().add(child);
        this.getChildren().sort(Comparator.comparing(SuggestionKeyNode::getText));
    }

    public static String getSuggestion(SuggestionKeyNode node){
        if (node.getParent() != null && node.getParent().getLayerLevel() > 0){
            return getSuggestion(node.getParent()) + "." + node.getText();
        }
        return node.getText();
    }
}
