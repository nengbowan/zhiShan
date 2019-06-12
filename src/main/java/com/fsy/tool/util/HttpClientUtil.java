package com.fsy.tool.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;

import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class HttpClientUtil {

    public static String getOrPost( HttpRequestBase getOrPost , HttpClient httpClient){
        try{
            HttpResponse response = httpClient.execute(getOrPost);
            HttpEntity httpEntity = response.getEntity();
            String result = EntityUtils.toString(httpEntity , Charset.defaultCharset());
            return result;
        }catch (SocketTimeoutException socketTimeoutException){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            getOrPost(getOrPost , httpClient);
        }catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }
}
