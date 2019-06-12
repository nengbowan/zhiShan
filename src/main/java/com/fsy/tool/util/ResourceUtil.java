package com.fsy.tool.util;

import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ResourceUtil {
    public static String resource2String(Resource resource){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            StringBuffer content = new StringBuffer();
            String temp = "";
            while((temp = br.readLine()) != null){
                content.append(temp);
            }
            return content.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
