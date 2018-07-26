package com.imooc.miaosha.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {


    /**
     * 寻找字符串中匹配正则表达式的部分
     *
     * @param pattern
     * @param string
     * @return
     */
    public static String findMatch(String pattern, String string) {
//        String pattern = "https://[^POST]+/v1/payments/payment.+parent_payment";
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        // 现在创建 matcher 对象
        Matcher m = r.matcher(string);
        if (m.find()) {
//            System.out.println("FIND MATCH: " + m.group(0));
            return m.group(0);
        } else {
//            System.out.println("NO MATCH");
            return null;
        }
    }

}
