package com.wangl.spring.suggestion;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @ClassName SuggestionNodeTree
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/30 14:27
 */
public class SuggestionNodeTree {

    private SuggestionKeyNode virtualRoot = SuggestionKeyNode.newInstance("", null);

    public List<SuggestionKeyNode> getRootNodes(){
        return virtualRoot.getChildren();
    }

    public SuggestionKeyNode insertNode(String prefix, PsiElement psiElement, SuggestionNodeType type){
        String[] nodes = prefix.split("\\.");
        SuggestionKeyNode suggestionKeyNode = recursionInsertNode(virtualRoot, nodes, 0);
        suggestionKeyNode.setPsiElement(psiElement);
        suggestionKeyNode.setType(type);
        return suggestionKeyNode;
    }

    private SuggestionKeyNode recursionInsertNode(SuggestionKeyNode parent, String[] prefix, int level){
        if (level == prefix.length){
            return parent;
        }
        Optional<SuggestionKeyNode> keyNodeOptional = parent.getChildren().stream()
                .filter(node -> node.getText().equals(prefix[level]))
                .findFirst();
        if (keyNodeOptional.isPresent()){
            SuggestionKeyNode currentNode = keyNodeOptional.get();
            return recursionInsertNode(currentNode, prefix, level + 1);
        }else {
            SuggestionKeyNode currentCreatedNode = SuggestionKeyNode.newInstance(prefix[level], parent);
            parent.addChild(currentCreatedNode);
            currentCreatedNode.setLayerLevel(level + 1);
            return recursionInsertNode(currentCreatedNode, prefix, level + 1);
        }
    }

    public SuggestionKeyNode searchNode(String prefix){
        String[] nodes = prefix.split("\\.");
        return recursionQueryNode(virtualRoot, nodes, 0);
    }

    private SuggestionKeyNode recursionQueryNode(SuggestionKeyNode parent, String[] prefix, int level){
        if (level == prefix.length){
            return parent;
        }
        Optional<SuggestionKeyNode> keyNodeOptional = parent.getChildren().stream()
                .filter(node -> node.getText().equals(prefix[level]))
                .findFirst();
        if (keyNodeOptional.isPresent()){
            SuggestionKeyNode currentNode = keyNodeOptional.get();
            return recursionQueryNode(currentNode, prefix, level + 1);
        }

        return null;
    }
}
