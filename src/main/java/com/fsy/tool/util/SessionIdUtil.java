package com.fsy.tool.util;

import java.util.Date;

public class SessionIdUtil {
    public static String tokenify(long number) {

        /*原js
         * function(number) { var tokenbuf = []; var charmap =
         * "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ*$";
         * var remainder = number; while (remainder > 0) {
         * tokenbuf.push(charmap.charAt(remainder & 0x3F)); remainder =
         * Math.floor(remainder / 64); } return tokenbuf.join(""); }
         */
        String tokenbuf = "";
        String charmap = "1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ*$";
        long remainder = number;
        while (remainder > 0) {
            tokenbuf = tokenbuf + charmap.charAt((int) (remainder) & 0x3F);
            remainder = (long) Math.floor(remainder / 64);
        }
        return tokenbuf;
    }

    public static String getScriptSessionId(String DWRSESSIONID) {
        /*原js
         * _pageId = tokenify(new Date().getTime()) + "-" +
         * tokenify(Math.random() * 1E16);
         * ScriptSessionId=DWRSESSIONID+"/"+_pageId;
         */
        String ScriptSessionId;
        ScriptSessionId = DWRSESSIONID + "/";
        ScriptSessionId = ScriptSessionId + tokenify(new Date().getTime()) + "-"
                + tokenify((long) (Math.random() * 1E16));
        return ScriptSessionId;
    }


}
