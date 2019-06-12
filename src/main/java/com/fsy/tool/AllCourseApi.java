package com.fsy.tool;

import com.fsy.tool.dto.CourseModelBean;
import com.fsy.tool.util.CollectionUtil;
import com.fsy.tool.util.HttpClientUtil;
import com.fsy.tool.util.SessionIdUtil;
import com.fsy.tool.util.StringUtil;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单用户的所有课程
 */
public class AllCourseApi {

    private String username;

    private String password;
    public AllCourseApi(String username , String password ){
        this.username = username;
        this.password = password;
    }
    private CookieStore cookieStore = new BasicCookieStore();
    private HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();

    private List<CourseModelBean> courseList = new ArrayList<>();

    private String DWRSESSIONID = null;

    private String rand = "2221";

    private PrintStream print = System.out;
    /**
     * 获取ScriptSessionId 登录需要
     */
    public void loginBefore(){

        getScriptSessionIdBeforeLogin();

    }

    /**
     * 登录
     */
    public void login(){
        loginBefore();
        doLogin();

    }

    private void doLogin() {
        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.coreAjax.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer","http://www.attop.com/login_pop.htm");
        post.addHeader("Content-Type","application/x-www-form-urlencoded");
        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=coreAjax\nc0-id=0\nc0-param0=string:loginWeb\nc0-e1=string:Username\nc0-e2=string:Password\nc0-e3=string:Rand\nc0-e4=number:2\nc0-param1=Object_Object:{username:reference:c0-e1, password:reference:c0-e2, rand:reference:c0-e3, autoflag:reference:c0-e4}\nc0-param2=string:doLogin\nbatchId=1\ninstanceId=0\npage=%2Flogin_pop.htm\nscriptSessionId=";
        postDataFormat = postDataFormat.replace("Username", username);
        postDataFormat = postDataFormat.replace("Password",password);
        postDataFormat = postDataFormat.replace("Rand" , rand);
        redbuildCookie();
        String postData = postDataFormat + getScriptSessionId();

        try {
            post.setEntity(new StringEntity(postData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        HttpClientUtil.getOrPost(post  , client);
    }

    public String getScriptSessionId() {

        if(StringUtil.isEmpty(DWRSESSIONID)){
            print.println("获取DWRSESSIONID失败，请检查网络!");
            System.exit(0);
        }

        return SessionIdUtil.getScriptSessionId(DWRSESSIONID) ;

    }

    private void redbuildCookie() {
        Cookie randCookie = new BasicClientCookie("rand",rand);
        ((BasicClientCookie) randCookie).setDomain("www.attop.com");
        cookieStore.addCookie(randCookie);
    }

    private void getScriptSessionIdBeforeLogin() {
        String url = "http://www.attop.com/js/ajax/call/plaincall/__System.generateId.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer" , "http://www.attop.com/index.htm");
        post.addHeader("Content-Type","application/x-www-form-urlencoded");
        String postData = "callCount=1\nc0-scriptName=__System\nc0-methodName=generateId\nc0-id=0\nbatchId=0\ninstanceId=0\npage=%2Findex.htm\nscriptSessionId=\nwindowName=\n";

        try {
            post.setEntity(new StringEntity(postData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String containSessionIdHtml = HttpClientUtil.getOrPost(post , client);

        this.DWRSESSIONID = StringUtil.subString(containSessionIdHtml, "r.handleCallback(\"0\",\"0\",\"", "\");");
    }
    public void start(){
        login();
        getCourseList();

        //委托CourseApi执行单个course
        if(CollectionUtil.isNotEmpty(courseList)){
            for(CourseModelBean course : courseList){
                new CourseApi(course.getCode() , course.getName(),  DWRSESSIONID  , client).start();
            }
        }
    }







    public void getCourseList(){
        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.commonAjax.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer" , "http://www.attop.com/user/study.htm");
        post.addHeader("Content-Type","application/x-www-form-urlencoded");
        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=commonAjax\nc0-id=0\nc0-param0=string:getAjaxList\nc0-e1=string:\nc0-e2=string:study.htm\nc0-e3=number:1\nc0-e4=string:showajaxinfo\nc0-param1=Object_Object:{param:reference:c0-e1, pagename:reference:c0-e2, currentpage:reference:c0-e3, showmsg:reference:c0-e4}\nc0-param2=string:doGetAjaxList\nbatchId=2\ninstanceId=0\npage=%2Fuser%2Fstudy.htm\nscriptSessionId=";

        String postData =postDataFormat + SessionIdUtil.getScriptSessionId(DWRSESSIONID);
        try {
            post.setEntity(new StringEntity(postData));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String encodeCourseListHtml =  HttpClientUtil.getOrPost(post , client);
        String courseListhtml = StringUtil.Unicode2Chinese(encodeCourseListHtml);



        String TD = "";//一列
        String TR = "";//一行
        while (true) {
            TR = StringUtil.subString(courseListhtml, "<tr>\\r\\n                <td>", "</tr>\\r\\n", "TRpos");//取出一行
            if ("".equals(TR)){
                break;
            }
            String code = StringUtil.subString(TR, "", "</td>\\r\\n", "TDpos");
            String name = StringUtil.subString(TR, "target=\\\"_blank\\\">", "</a></td>", "TDpos");
            String score = StringUtil.subString(TR, "</a></td>\\r\\n                <td>", "</td>\\r\\n                <td>", "TDpos");
            String limitTime = StringUtil.subString(TR, score+"</td>\\r\\n                <td>", "</td>\\r\\n                <td>", "TDpos");
            String status = StringUtil.subString(TR, limitTime + "</td>\\r\\n                <td>", "</td>\\r\\n                <td><a", "TDpos");
            courseListhtml = courseListhtml.replace(TR , "");
            courseList.add(
                    CourseModelBean.builder()
                    .code(code)
                    .name(name)
                    .score(score)
                    .limitTime(limitTime)
                    .status(status)
                    .build()
            );
        }
        return;

    }

    public void setCourseList(List<CourseModelBean> courseList) {
        this.courseList = courseList;
    }
}
