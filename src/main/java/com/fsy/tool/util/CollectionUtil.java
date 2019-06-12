package com.fsy.tool.util;

import java.util.List;

public class CollectionUtil {
    public static boolean isEmpty(List list){
        return list == null || list.size() == 0;
    }

    public static boolean isNotEmpty(List list){
        return list != null && list.size() != 0;
    }
}
