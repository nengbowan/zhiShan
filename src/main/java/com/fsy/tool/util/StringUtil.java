package com.fsy.tool.util;

public class StringUtil {

    public static int TRpos = 0;// RefreshCourse取每科目的position
    public static int TDpos = 0;// RefreshCourse取每科的id名称

    public static int zhang_GetSection = 0;   //GetSection取每个课程的章节
    public static int jie_GetSection = 0;     //GetSection取每个课程章下面具体的节数


    public static int pos_MediaPj = 0;  //媒体评价截取index


    public static int iPos_InitPostTime = 0; //课程挂时间截取index

    public static boolean isEmpty(String text){
        return text == null || text.equals("");
    }
    public static String subString(String text, String a, String b) {
        int beginIndex = text.indexOf(a);
        int endIndex = text.indexOf(b, beginIndex + a.length());
        return text.substring(beginIndex + a.length(), endIndex);
    }

    public static String subString(String text, String a, String b, String type) {
        int beginIndex = 0;
        if (type.equals("TRpos")) {
            beginIndex = text.indexOf(a, 0);
        } else if (type.equals("TDpos")) {
            beginIndex = text.indexOf(a, 0);
        } else if (type.equals("zhang_GetSection")) {
            beginIndex = text.indexOf(a, 0);
        } else if (type.equals("jie_GetSection")) {
            beginIndex = text.indexOf(a, 0);
        } else if (type.equals("pos_MediaPj")) {
            beginIndex = text.indexOf(a, 0);
        } else if (type.equals("iPos_InitPostTime")) {
            beginIndex = text.indexOf(a, 0);
        }

        if (beginIndex == -1) {
            return "";
        }
        int endIndex = text.indexOf(b, beginIndex + a.length());


//        if (type.equals("TRpos")) {
//            TRpos = endIndex;
//        } else if (type.equals("TDpos")) {
//            TDpos = endIndex;
//        } else if (type.equals("zhang_GetSection")) {
//            zhang_GetSection = endIndex;
//        } else if (type.equals("jie_GetSection")) {
//            jie_GetSection = endIndex;
//        } else if (type.equals("pos_MediaPj")) {
//            pos_MediaPj = endIndex;
//        } else if (type.equals("iPos_InitPostTime")) {
//            iPos_InitPostTime = endIndex;
//        }
        if (endIndex == -1) {
            return "";
        }
        return text.substring(beginIndex + a.length(), endIndex);
    }

    /**
     * 将Unicode转换为中文
     *
     * @param utfString
     * @return
     */
    public static String Unicode2Chinese(String utfString) {
        StringBuilder sb = new StringBuilder();
        int i = -1;
        int pos = 0;
        while ((i = utfString.indexOf("\\u", pos)) != -1) {
            sb.append(utfString.substring(pos, i));
            if (i + 5 < utfString.length()) {
                pos = i + 6;
                sb.append((char) Integer.parseInt(utfString.substring(i + 2, i + 6), 16));
            }
        }
        return sb.toString();
    }
}
