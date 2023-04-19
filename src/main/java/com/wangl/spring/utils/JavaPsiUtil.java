package com.wangl.spring.utils;

import com.intellij.psi.PsiType;

/**
 * @ClassName JavaPsiUtil
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/27 13:10
 */
public class JavaPsiUtil {

    public static boolean isPrimitiveType(PsiType psiType){
        String canonicalText = psiType.getCanonicalText();
        return canonicalText.equals("byte") || canonicalText.equals("java.lang.Byte") ||
                canonicalText.equals("short") || canonicalText.equals("java.lang.Short") ||
                canonicalText.equals("int") || canonicalText.equals("java.lang.Integer") ||
                canonicalText.equals("long") || canonicalText.equals("java.lang.Long") ||
                canonicalText.equals("float") || canonicalText.equals("java.lang.Float") ||
                canonicalText.equals("double") || canonicalText.equals("java.lang.Double") ||
                canonicalText.equals("boolean") || canonicalText.equals("java.lang.Boolean") ||
                canonicalText.equals("char") || canonicalText.equals("java.lang.Character");
    }
}
