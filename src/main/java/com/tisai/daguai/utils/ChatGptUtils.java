package com.tisai.daguai.utils;

import com.alibaba.fastjson.JSON;
import com.tisai.daguai.common.GPT;
import com.tisai.daguai.common.Message;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ChatGptUtils {

        private static final String API_KEY = "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";
        private static final String API_URL = "https://api.openai.com/v1/chat/completions";

        public static String reply(String question) throws Exception {
            OkHttpClient client = new OkHttpClient();

            MediaType mediaType = MediaType.parse("application/json");

            //设置post请求体
            GPT gpt = new GPT("gpt-3.5-turbo",1024);
            Message message = new Message("user",question);
            List<Message> messages = Collections.singletonList(message);
            gpt.setMessages(messages);
            String jsonString = JSON.toJSONString(gpt);


            @SuppressWarnings("deprecation") RequestBody body = RequestBody.create(mediaType, jsonString);
            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .build();

            Response response = client.newCall(request).execute();
            String responseBody = Objects.requireNonNull(response.body()).string();

//            System.out.println(responseBody);

            //单条消息剪切
            int endIndex = responseBody.lastIndexOf("finish_reason")-4;
            int beginIndex = responseBody.lastIndexOf(":", endIndex)+2;
            String reply = responseBody.substring(beginIndex,endIndex);

////            reply = reply.("\\n", "<br>");
//            //设置要匹配的字符串
//            String regex="\\n";
//            //设置pattern类
//            Pattern pattern = Pattern.compile(regex);
//            //设置原字符串matcher
//            Matcher matcher=pattern.matcher(reply);
//            //设置目标替换字符串
//            String toString="<br>";
//            //开始全部替换
//            reply = matcher.replaceAll(toString);
            log.info(reply);
            return reply;
        }
}
