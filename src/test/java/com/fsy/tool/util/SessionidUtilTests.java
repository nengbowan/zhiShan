package com.fsy.tool.util;

import java.util.Date;

public class SessionidUtilTests {
    public static void main(String[] args) {
        System.out.println(SessionIdUtil.tokenify(new Date().getTime()) + "-"
                + SessionIdUtil.tokenify((long) (Math.random() * 1E16)));

    }
}
