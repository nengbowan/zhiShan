package com.fsy.tool;

import com.fsy.tool.dto.SectionModelBean;
import com.fsy.tool.util.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static javax.swing.UIManager.getString;

public class CourseApi{

    private String courseId;

    private String courseName;

    private String DWRSESSIONID = null;

    private String ANSWER_SUFFIX = "txt";

    /*　未完成的章节 */
    private List<SectionModelBean> sectionQueue = new ArrayList<>();

    //以课程　章节　挂机时间　为key 做缓存
    private Map<String,String> sectionStatusMap = new HashMap<>();


    private String courseAnswer = null;
    public CourseApi(String courseId ,String courseName , String DWRSESSIONID ,  HttpClient client){
        this.courseId = courseId;
        this.courseName = courseName;
        this.client = client;
        this.DWRSESSIONID = DWRSESSIONID;

        //载入答案
        loadCourseAnswer();
    }

    private void loadCourseAnswer() {
        Resource inputSource = new ClassPathResource(courseId+"."+ANSWER_SUFFIX);
        courseAnswer = ResourceUtil.resource2String(inputSource);

        if(StringUtil.isEmpty(courseAnswer)){
            print.println("载入答案失败！编号:"+courseId);
            print.println("程序退出!");
            System.exit(0);
        }
    }

    private HttpClient client =  null;


    private List<String> secitonIds = new ArrayList();
    private List<String> secitonNames = new ArrayList();

    private PrintStream print = System.out;

    public void start(){
        getSection();
        filterHasCompleteSection();
        doLikeAndQuestion();
    }

    public String getScriptSessionId() {

        if(StringUtil.isEmpty(DWRSESSIONID)){
            print.println("获取DWRSESSIONID失败，请检查网络!");
            System.exit(0);
        }

        return SessionIdUtil.getScriptSessionId(DWRSESSIONID) ;

    }


    /**
     * 获取章节的学习状态,例如时间完成多少，题目完成多少，媒体评价完成多少
     *
     * @param CourseID
     * @param SectionID
     * @param batchId
     * @return
     */
    private String getSectionStatus(String CourseID, String SectionID, int batchId ) {

//        String cacheKey = CourseID  + SectionID + batchId ;
//
//        if(sectionStatusMap.containsKey(cacheKey)){
//            return sectionStatusMap.get(cacheKey);
//        }

        String t;
        String result;
        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.commonAjax.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer" , String.format("http://www.attop.com/wk/learn.htm?id=%s&jid=%s" , CourseID , SectionID) );
        post.addHeader("Content-Type","application/x-www-form-urlencoded");
        t = "" + batchId;

        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=commonAjax\nc0-id=0\nc0-param0=string:getAjaxList2\nc0-e1=string:id%3DCourseID%26jid%3DSectionID\nc0-e2=string:learn_1.htm\nc0-e3=number:1\nc0-e4=string:showajaxinfo2\nc0-param1=Object_Object:{param:reference:c0-e1, pagename:reference:c0-e2, currentpage:reference:c0-e3, id:reference:c0-e4}\nc0-param2=string:doShowAjaxList2\nbatchId=BatchID\ninstanceId=0\npage=%2Fwk%2Flearn.htm%3Fid%3DCourseID\nscriptSessionId=";
        postDataFormat = postDataFormat.replace("CourseID", CourseID);
        postDataFormat = postDataFormat.replace("SectionID", SectionID);
        postDataFormat = postDataFormat.replace("BatchID", t);
        postDataFormat += getScriptSessionId();

        try {
            post.setEntity(new StringEntity(postDataFormat));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encodeHtml = HttpClientUtil.getOrPost(post , client);

        String decodeHtml = StringUtil.Unicode2Chinese(encodeHtml);

//        sectionStatusMap.put(cacheKey , decodeHtml);
        return decodeHtml;
    }

    private void filterHasCompleteSection() {
        for (int i = 0; i < secitonIds.size(); i++) {
            String sectionID = secitonIds.get(i);
            String sectionName = secitonNames.get(i);
            String status = getSectionStatus(courseId, sectionID, 1);
            if (!status.contains("OK</strong> \\r\\n                 <span class=\\\"explain_rate\\\"><a href=\\\"javascript:;\\\" onclick=\\\"atPage(\\\'时间说明")) {
                /*时间没挂完，加入待挂队列*/
                sectionQueue.add(
                        SectionModelBean.builder()
                                .courseId(courseId)
                                .courseName(courseName)
                                .sectionId(sectionID)
                                .sectionName(sectionName)
                                .build());
            }

        }
    }

    public boolean getSection() {
        String url = "http://www.attop.com/wk/learn.htm?id=" + courseId;
        HttpGet getSectionsGet = new HttpGet(url);
        String sectionListHtml = HttpClientUtil.getOrPost(getSectionsGet , client);
        if (sectionListHtml.equals("wrong")) {
            print.println("网络不太好" +  "网络错误" +  "\n\n网络不太好，请在良好的网络环境下重试\n");
            return false;
        }
        //页面错误
        if (!sectionListHtml.contains("个人中心")) {
            print.println("网络不太好"+ "网络错误" + "\n\n不好意思，在挂下一门课的时候发生了未知错误，可能是你断网了，请重试！\n");
            return false;
        }
        int startIndex = 0;
        while (true) {
            //用户缓存被替换之前的zhang
            String tempZhang = null;

            String zhang = StringUtil.subString(sectionListHtml, "<dt name=\"zj\"", "</dd>", "zhang_GetSection");
            if(startIndex == 0){
                tempZhang = new String(zhang);
            }
            if (zhang.isEmpty()) {
                break;
            }
            startIndex = startIndex + 1;
            while (true) {
                String jie = StringUtil.subString(zhang, "<li", "</li>", "jie_GetSection");
                if (jie.isEmpty()) {
                    startIndex = 0;
                    break;
                }
                String temp;
                temp = StringUtil.subString(zhang, "span title=\"", "</span>");
                temp = temp.substring(0, 4);//取出第几章
                secitonIds.add(StringUtil.subString(jie, "id=\"j_", "\">"));
                secitonNames.add(temp + " " + StringUtil.subString(jie, "title=\"", "\""));

                //去掉已经添加成功的　防止重复添加章节id
                zhang = zhang.replace("<li"+jie + "</li>", "");
            }
            //去掉已经添加成功的　防止重复添加章节id

            sectionListHtml = sectionListHtml.replace("<dt name=\"zj\"" + tempZhang + "</dd>" , "");


        }
        return true;
    }

    private void doLikeAndQuestion() {
        if(CollectionUtil.isNotEmpty(sectionQueue)){
            if(CollectionUtil.isNotEmpty(sectionQueue)){
                for(SectionModelBean sectionModelBean : sectionQueue){
                    String sectionID = sectionModelBean.getSectionId();
                    String sectionName = sectionModelBean.getSectionName();
                    ThreadPostExes(courseId, sectionID, sectionName);
                }

                print.println("习题　点赞　完成　～　开始刷时间");
                for (SectionModelBean sectionModelBean : sectionQueue) {
                    String sectionID = sectionModelBean.getSectionId();
                    String sectionName = sectionModelBean.getSectionName();
                    postTime(courseId  , courseName , sectionID , sectionName);
                }

            }
        }

    }

    /**
     * 挂时间
     * @param courseID
     * @param courseName
     * @param sectionID
     * @param sectionName
     */
    private void postTime(String courseID, String courseName, String sectionID, String sectionName) {
        String result;
        int batchId = 1;
        String LastTime = "---";//上次剩余时间
        while (true) {

            result = getSectionStatus(courseID, sectionID, batchId  );
            if (result.contains(
                    "OK</strong> \\r\\n                 <span class=\\\"explain_rate\\\"><a href=\\\"javascript:;\\\" onclick=\\\"atPage(\\\'时间说明")) {
                /*挂完了*/
                return;
            } else {
                String RemainTime = StringUtil.subString(result, "已学时间\\\">", "</span>");
                RemainTime = RemainTime + "/" + StringUtil.subString(result, "总学习时间\\\">", "</span>");
                if (!LastTime.equalsIgnoreCase(RemainTime)) {
                    LastTime = RemainTime;
                    print.println("剩余时间：" + sectionName + " " + RemainTime + "");
                    batchId++;
                }
                for (int i = 0; i < 15; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                doPostTime(courseID , courseName , sectionID , sectionName , batchId);

            }
            batchId = batchId + 1 ;

        }

    }

    private void doPostTime(String courseId ,String courseName ,  String sectionID , String sectionName , Integer batchId) {
        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.commonAjax.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer" , String.format("http://www.attop.com/wk/learn.htm?id=CourseID&jid=SectionID" , courseId , sectionID ));
        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=commonAjax\nc0-id=0\nc0-param0=string:getWkOnlineNum\nc0-e1=number:CourseID\nc0-e2=number:SectionID\nc0-param1=Object_Object:{bid:reference:c0-e1, jid:reference:c0-e2}\nc0-param2=string:doGetWkOnlineNum\nbatchId=BatchID\ninstanceId=0\npage=%2Fwk%2Flearn.htm%3Fid%3DCourseID%26jid%3DSectionID\nscriptSessionId=";
        postDataFormat = postDataFormat.replace("CourseID", courseId);
        postDataFormat = postDataFormat.replace("SectionID", sectionID);
        postDataFormat = postDataFormat.replace("BatchID", batchId + "");
        postDataFormat += getScriptSessionId();

        try {
            post.setEntity(new StringEntity(postDataFormat));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String result = HttpClientUtil.getOrPost(post , client);
        if (!result.contains("flag:1")) {
            print.println( "POST时间出错：" + courseName + " " + sectionName + "\n");
        } else {
            print.println("挂了15秒：" + sectionName + "\n");
        }
    }

    /**
     * 将某章的题进行提交
     *
     * @param courseID
     * @param sectionID
     * @param sectionName
     */
    private void ThreadPostExes(String courseID , String sectionID, String sectionName) {
        String Answer;//待提交的答案

        //本章课程学习的状态
        String status;
        boolean status_xiti = false;
        boolean status_meiti = false;

        status = getSectionStatus(courseID, sectionID, 1);

        if (status.contains("OK</strong> \\r\\n                 <span class=\\\"explain_rate\\\"><a href=\\\"javascript:;\\\" onclick=\\\"atPage(\\\'习题说明")) {
            /*习题挂完了*/
            status_xiti = true;
        }

        /*开始获取本节的所有内容，包括媒体评价和习题*/

        String returnResult = getLikeAndQuestionHtml(courseID , sectionID);

        //必须先点击章节连接　才能看课 否则不能看课
        openSection(courseID , sectionID);

        List<String> medias = new ArrayList<>();
        if (status_meiti != true) {
            while (true) {
                String mediaId = StringUtil.subString(returnResult, "parent.showMediaRight(", ")", "pos_MediaPj");
                medias.add(mediaId);
                returnResult = returnResult.replace( "parent.showMediaRight("+ mediaId + ")" , "");
                if (mediaId.isEmpty()) {
                    break;
                }
            }
            MediaPj(returnResult, courseName, sectionName , medias);
        }



        returnResult = StringUtil.subString(returnResult, "<span class=\\\"delNum", "</dd>");

        returnResult = StringUtil.Unicode2Chinese(returnResult);

        /*提交本章的题库*/
        Answer = StringUtil.subString(courseAnswer, "<" + sectionID + ">", "</" + sectionID + ">");

        if (returnResult.contains("正确率：100%") || status_xiti == true) {
            print.println("本节题已做完：" + courseName + " " + sectionName + "\n");
            return;
        }

        //有题库,开始答题
        if (!Answer.isEmpty()) {

            String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.dotAjax.dwr";

            HttpPost post = new HttpPost(url);

            post.addHeader("Referer" , String.format("http://www.attop.com/wk/learn.htm?id=%s&jid=%s" , courseID , sectionID) );
            post.addHeader("Content-Type","application/x-www-form-urlencoded");


            //UFT-8 to URL
            String t = "";
            try {
                Answer = new String(Answer.getBytes(), "utf-8");
                t = java.net.URLEncoder.encode(Answer, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=dotAjax\nc0-id=0\nc0-param0=string:doSubmitWkXtAll\nc0-e1=number:CourseID\nc0-e2=number:SectionID\nc0-e3=string:Answer\nc0-param1=Object_Object:{bid:reference:c0-e1, jid:reference:c0-e2, msg:reference:c0-e3}\nc0-param2=string:doCommonReturn\nbatchId=4\ninstanceId=0\npage=%2Fwk%2Flearn.htm%3Fid%3DCourseID\nscriptSessionId=";
            postDataFormat = postDataFormat.replace("Answer", t);
            postDataFormat = postDataFormat.replace("CourseID", courseID);
            postDataFormat = postDataFormat.replace("SectionID", sectionID);
            String postData = postDataFormat + getScriptSessionId();

            try {
                post.setEntity(new StringEntity(postData));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            String resp = HttpClientUtil.getOrPost(post , client);

            print.println("该节题库已提交：" + courseName + " " + sectionName );


        } else {
            print.println("该节无题库" + "无题库：" + courseName + " " + sectionName + "\n");
        }

    }

    private void openSection(String courseId , String sectionId) {
        String url = "http://www.attop.com/wk/learn.htm?id=%s&jid=%s";
        url = String.format(url , courseId , sectionId);
        HttpGet get = new HttpGet(url);
        HttpClientUtil.getOrPost(get , client);

    }

    private String getLikeAndQuestionHtml(String courseID , String sectionID) {

        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.commonAjax.dwr";

        HttpPost post = new HttpPost(url);

        post.addHeader("Referer" , String.format("http://www.attop.com/wk/learn.htm?id=CourseID&jid=SectionID" , courseId ,sectionID));
        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=commonAjax\nc0-id=0\nc0-param0=string:getAjaxList\nc0-e1=string:id%3DCourseID%26jid%3DSectionID\nc0-e2=string:learn.htm\nc0-e3=number:1\nc0-e4=string:showajaxinfo\nc0-param1=Object_Object:{param:reference:c0-e1, pagename:reference:c0-e2, currentpage:reference:c0-e3, showmsg:reference:c0-e4}\nc0-param2=string:doGetAjaxList\nbatchId=2\ninstanceId=0\npage=%2Fwk%2Flearn.htm%3Fid%3DCourseID\nscriptSessionId=";
        postDataFormat = postDataFormat.replace("CourseID", courseID);
        postDataFormat = postDataFormat.replace("SectionID", sectionID);
        postDataFormat += getScriptSessionId();

        try {
            post.setEntity(new StringEntity(postDataFormat));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return HttpClientUtil.getOrPost(post , client);
    }

    /**
     * 完成单个的点赞
     * @param mediaId
     */
    public void doMedia(String mediaId,String sectionName){

        //服务器规定提交评价需要隔一段时间再评价　否则返回失败
//        sleep();
//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        String url = "http://www.attop.com/js/ajax/call/plaincall/zsClass.dotAjax.dwr";
        HttpPost post = new HttpPost(url);
        post.addHeader("Referer",String.format("http://www.attop.com/wk/media_pop.htm?id=%s" , mediaId));
        post.addHeader("Content-Type","text/plain");

        String postDataFormat = "callCount=1\nwindowName=\nc0-scriptName=zsClass\nc0-methodName=dotAjax\nc0-id=0\nc0-param0=string:doWkMediaPj\nc0-e1=number:MediaID\nc0-e2=number:3\nc0-param1=Object_Object:{id:reference:c0-e1, type:reference:c0-e2}\nc0-param2=string:doWkMediaPj\nbatchId=1\ninstanceId=0\npage=%2Fwk%2Fmedia_pop.htm%3Fid%3DMediaID\nscriptSessionId=";
        postDataFormat = postDataFormat.replace("MediaID", mediaId);
        postDataFormat += getScriptSessionId();

        print.println("准备提交：" + " " + sectionName + " 媒体ID->" + mediaId );


        try {
            post.setEntity(new StringEntity(postDataFormat));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000).setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000).build();

        post.setConfig(requestConfig);
        String resp = HttpClientUtil.getOrPost(post , client);
        //flag:131 失败 媒体已经评价
        //flag:1 成功
        //flag:0 失败
        if(resp.contains("flag:131")){
            print.println("媒体已经评价!");
        }else if(resp.contains("flag:1")){
            print.println("媒体评价提交成功");
        }
        else{
            print.println("媒体评价提交失败\n返回:"+resp);
        }


    }

    public int GetRandInt(int m, int n) {
        Random rand = new Random();
        return m + rand.nextInt(n - m);
    }

    private void sleep() {
        int time = GetRandInt(2, 5);
        for (int j = 0; j < time; j++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对媒体进行评价
     *
     * @param returnResult
     * @param courseName
     * @param sectionName
     * @return
     */
    private void MediaPj(String returnResult, String courseName, String sectionName ,List<String> mediaIds) {
        print.println("提交媒体评价：" + " " + sectionName + "\n");
        if(CollectionUtil.isNotEmpty(mediaIds)){
            for(String mediaId : mediaIds){
                if(!"".equals(mediaId)){
                    doMedia(mediaId , sectionName);
                }
            }
        }
    }

}
