package com.mlinyun.onlinecourse.basics.utils;

import java.util.Objects;

/**
 * 判断为空工具类
 */
public class NullUtils {
    public static boolean isNull(String str) {
        return str == null || Objects.equals("", str) || Objects.equals("null", str) || Objects.equals("undefined", str);
    }
}
