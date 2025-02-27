package top.kimwonjoon.sdk.test;

import com.alibaba.fastjson2.JSON;
import org.junit.Test;
import top.kimwonjoon.sdk.domain.model.ChatCompletionSyncResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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
}
