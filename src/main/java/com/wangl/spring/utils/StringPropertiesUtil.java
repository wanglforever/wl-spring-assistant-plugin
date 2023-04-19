package com.wangl.spring.utils;

import com.intellij.openapi.util.text.StringUtil;

/**
 * @ClassName StringPropertiesUtil
 * @Description TODO
 * @Author wangl
 * @Date 2023/3/30 13:15
 */
public class StringPropertiesUtil {

    public static String toCamelCase(String propertyName, String delimiter){
        String[] strings = propertyName.split(delimiter);
        StringBuilder sb = new StringBuilder(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append(StringUtil.capitalize(strings[i]));
        }
        return sb.toString();
    }
}
