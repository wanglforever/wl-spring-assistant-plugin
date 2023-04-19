package com.wangl.spring.utils;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.psi.util.PsiUtil;
import com.wangl.spring.suggestion.SuggestionNodeTree;
import com.wangl.spring.suggestion.SuggestionNodeType;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName SpringBootPropertiesResolver
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/24 15:58
 */
public class SpringBootPropertiesResolver {

    public static boolean isAnnotatedConfigurationProperties(PsiClass psiClass) {
        PsiAnnotation configurationPropertiesAnnotation = psiClass.getAnnotation(Annotations.CONFIGURATION_PROPERTIES);
        return configurationPropertiesAnnotation != null;
    }

    public static boolean isAnnotatedConfigurationProperties(PsiMethod method){
        return method.getAnnotation(Annotations.BEAN) != null &&
                method.getAnnotation(Annotations.CONFIGURATION_PROPERTIES) != null &&
                method.hasModifierProperty(PsiModifier.PUBLIC) &&
                !method.hasModifierProperty(PsiModifier.STATIC) &&
                !method.hasParameters() &&
                !PsiType.VOID.equals(method.getReturnType());
    }

    public static boolean isConfiguration(PsiClass psiClass){
        PsiAnnotation configuration = psiClass.getAnnotation(Annotations.CONFIGURATION);
        return configuration != null;
    }

    public static void analysisConfigurationClass(PsiClass psiClass, SuggestionNodeTree suggestionNodeTree) {
        List<PsiMethod> methods = Arrays.stream(psiClass.getAllMethods())
                .filter(SpringBootPropertiesResolver::isAnnotatedConfigurationProperties)
                .collect(Collectors.toList());

        for (PsiMethod method : methods) {
            PsiAnnotation configurationProperties = method.getAnnotation(Annotations.CONFIGURATION_PROPERTIES);
            assert configurationProperties != null;
            String prefixValue = getPrefixValue(configurationProperties);
            if (StringUtils.isBlank(prefixValue)){
                continue;
            }
            suggestionNodeTree.insertNode(prefixValue, method, SuggestionNodeType.KNOWN_CLASS);
            PsiType returnType = method.getReturnType();
            if (returnType instanceof PsiClassType){
                PsiClassType returnClassType = (PsiClassType) returnType;
                recursionAnalysisPrefix(PsiCustomUtil.toValidPsiClass(returnClassType), prefixValue, suggestionNodeTree);
            }
        }
    }

    public static void analysisConfigurationPropertiesClass(PsiClass aClass, SuggestionNodeTree suggestionNodeTree){
        PsiAnnotation configurationPropertiesAnnotation = aClass.getAnnotation(Annotations.CONFIGURATION_PROPERTIES);
        assert configurationPropertiesAnnotation != null;
        String prefixValue = getPrefixValue(configurationPropertiesAnnotation);
        if (StringUtils.isNotBlank(prefixValue)){
            suggestionNodeTree.insertNode(prefixValue, aClass, SuggestionNodeType.KNOWN_CLASS);
            recursionAnalysisPrefix(aClass, prefixValue, suggestionNodeTree);
        }
    }

    private static void recursionAnalysisPrefix(PsiClass psiClass, String prefixValue, SuggestionNodeTree suggestionNodeTree){
        if (psiClass == null) return;
        PsiMethod[] allMethods = psiClass.getAllMethods();
        for (PsiMethod method : allMethods) {
            if (PsiCustomUtil.isMethodOfObjectClass(method)){
                continue;
            }
            PsiType propertyType = PsiCustomUtil.getSetterArgumentType(method);
            if (propertyType == null){
                continue;
            }
            String propertyName = PropertyUtilBase.getPropertyName(method);
            if (isApplicationPropertiesKey(propertyType)){
                suggestionNodeTree.insertNode(prefixValue + "." + propertyName, method, SuggestionNodeType.PRIMITIVE_STRING);
                continue;
            }
            PsiClass propertyPsiClass = PsiUtil.resolveClassInType(propertyType);
            if (propertyPsiClass == null){
                continue;
            }
            if (propertyType instanceof PsiClassType && isNonGenericType((PsiClassType) propertyType)){
                suggestionNodeTree.insertNode(prefixValue + "." + propertyName, method, SuggestionNodeType.KNOWN_CLASS);
                recursionAnalysisPrefix(propertyPsiClass, prefixValue + "." + propertyName, suggestionNodeTree);
            }else if (propertyType instanceof PsiClassType && PsiCustomUtil.isIterable(propertyPsiClass)){
                PsiType parameterPsiType = ((PsiClassType) propertyType).getParameters()[0];
                suggestionNodeTree.insertNode(prefixValue + "." + propertyName, method, SuggestionNodeType.ITERABLE);
                recursionAnalysisPrefix(PsiUtil.resolveClassInType(parameterPsiType), prefixValue + "." + propertyName, suggestionNodeTree);
            }
        }
    }

    public static boolean isNonGenericType(PsiClassType classType) {
        PsiType[] parameters = classType.getParameters();
        return parameters.length == 0 && !classType.isRaw();
    }

    private static boolean isApplicationPropertiesKey(PsiType type){
        SuggestionNodeType suggestionNodeType = PsiCustomUtil.getSuggestionNodeType(type);
        if (suggestionNodeType == SuggestionNodeType.UNKNOWN_CLASS){
            return false;
        }
        if (suggestionNodeType.representsPrimitiveOrString()){
            return true;
        }
        if (suggestionNodeType == SuggestionNodeType.ITERABLE || suggestionNodeType == SuggestionNodeType.MAP){
            Map<PsiTypeParameter, PsiType> typeParameters = PsiCustomUtil.getTypeParameters(type);
            if (typeParameters == null){
                return false;
            }
            return typeParameters.values().stream()
                    .allMatch(t -> PsiCustomUtil.getSuggestionNodeType(t).representsPrimitiveOrString());
        }
        return false;
    }

    private static String getPrefixValue(PsiAnnotation configurationPropertiesAnnotation){
        PsiAnnotationMemberValue prefix = configurationPropertiesAnnotation.findAttributeValue("prefix");
        if (prefix instanceof PsiLiteralExpression){
            String prefixStr = Objects.requireNonNull(((PsiLiteralExpression) prefix).getValue()).toString();
            if (StringUtils.isBlank(prefixStr)){
                PsiAnnotationMemberValue value = configurationPropertiesAnnotation.findAttributeValue("value");
                if (value instanceof PsiLiteralExpression){
                    prefixStr = Objects.requireNonNull(((PsiLiteralExpression) value).getValue()).toString();
                }
            }
            return prefixStr;
        }
        return "";
    }
}
