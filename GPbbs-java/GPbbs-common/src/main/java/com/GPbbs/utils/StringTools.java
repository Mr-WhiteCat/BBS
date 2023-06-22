package com.GPbbs.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class StringTools {

    // 判断是否为空
    public static boolean isEmpty(String str) {

        if ( null == str || "".equals(str.trim()) || "null".equals(str) || "\u0000".equals(str)) {
            return true;
        } else {
            return false;
        }
    }

    // 生成随机数
    public static final String getRandomNumber(Integer count) {
        return RandomStringUtils.random(count, false, true);
    }

    // 生成随机字符串
    public static final String getRandomString(Integer count) {
        return RandomStringUtils.random(count, true, true);
    }

    // MD5加密
    public static String encodeByMD5(String sourceStr) {
        return StringTools.isEmpty(sourceStr) ? null : DigestUtils.md5Hex(sourceStr);
    }

    // 获取后缀方法
    public static String getFileSuffix(String fileName){
        return fileName.substring((fileName.lastIndexOf(".")));
    }

    // 对文本内容进行转译， 防止xss
    public static String escapeHtml(String content) {
        if (isEmpty(content)) {
            return content;
        }
        content = content.replace("<", "&lt;");
        content = content.replace(" ", "&nbsp;");
        content = content.replace("\n", "<br>");
        return content;
    }

    public static String getFileName(String fileName){
        return fileName.substring(0,fileName.lastIndexOf("."));
    }

}
