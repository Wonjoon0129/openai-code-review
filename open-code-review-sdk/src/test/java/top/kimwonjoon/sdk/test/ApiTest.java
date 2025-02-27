package top.kimwonjoon.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import top.kimwonjoon.sdk.domain.model.ChatCompletionSyncResponse;
import top.kimwonjoon.sdk.domain.model.Message;
import top.kimwonjoon.sdk.types.utils.WXAccessTokenUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @ClassName ApiTest
 * @Description
 * @Author @kimwonjoon
 * @Date 2025/2/26 15:41
 * @Version 1.0
 */

public class ApiTest {
    public static void main(String[] args) {

    }
    @Test
    public void test_http() throws IOException {
        String apiKeySecret="sk-db2968be21644311a3ceca2de967552b";
        URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization","Bearer "+apiKeySecret);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setDoOutput(true);

        String code="1+1";
        String jsonInpuString = "{"
                + "\"model\": \"deepseek-r1-distill-qwen-1.5b\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为:"+code+"\""
                + "    }"
                + "]"
                + "}";
        System.out.println("1111111");
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }
        int responseCode = connection.getResponseCode();
        System.out.println(responseCode);
        System.out.println("111");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }

        in.close();
        connection.disconnect();

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());

    }
    @Test
    public void test_wx(){
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("peoject","big-market");
        message.put("review","feat: 新加功能");

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));


    }
    public static class Message {
        private String touser = "onQNU7Pd75a7FTlROGYZjMBzYoys";
        private String template_id = "OT106XJ9dBKRWFpSX1T84VeE_cIycD25ZspnEijGjss";
        private String url = "https://weixin.qq.com";
        private Map<String, Map<String, String>> data = new HashMap<>();

        public void put(String key, String value) {
            data.put(key, new HashMap<String, String>() {
                {
                    put("value", value);
                }
            });
        }

        public String getTouser() {
            return touser;
        }

        public void setTouser(String touser) {
            this.touser = touser;
        }

        public String getTemplate_id() {
            return template_id;
        }

        public void setTemplate_id(String template_id) {
            this.template_id = template_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, Map<String, String>> getData() {
            return data;
        }

        public void setData(Map<String, Map<String, String>> data) {
            this.data = data;
        }
    }
    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
