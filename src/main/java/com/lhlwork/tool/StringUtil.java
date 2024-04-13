package com.lhlwork.tool;

public class StringUtil {

    /**
     * 将驼峰命名的字符串转换为下划线样式
     *
     * @param camelCaseStr 驼峰命名字符串
     * @return 下划线样式字符串
     */
    public static String camelCaseToUnderscore(String camelCaseStr) {
        if (camelCaseStr == null || camelCaseStr.isEmpty()) {
            return camelCaseStr;
        }

        StringBuilder sb = new StringBuilder(camelCaseStr.length());
        char[] chars = camelCaseStr.toCharArray();

        for (char c : chars) {
            if (Character.isUpperCase(c)) {
                sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        // 移除字符串开头的下划线（如果有）
        if (sb.charAt(0) == '_') {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }

    /**
     * 判断字符串是否为空
     *
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     *
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}
