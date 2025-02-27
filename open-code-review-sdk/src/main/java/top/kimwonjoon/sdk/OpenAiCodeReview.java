package top.kimwonjoon.sdk;

import com.alibaba.fastjson2.JSON;
import top.kimwonjoon.sdk.domain.model.ChatCompletionSyncResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class OpenAiCodeReview {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("测试执行");

        //1.代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder diffCode=new StringBuilder();
        while((line=reader.readLine())!=null){
            diffCode.append(line);
        }

        int exitCode = process.waitFor();
        System.out.println("ExitCode:"+exitCode);
        System.out.println("diffcode"+diffCode.toString());
        //2.评审
        String log = codereview(diffCode.toString());
        System.out.println("code review"+log);

    }
    private static String codereview(String diffcode) throws IOException {
        String apiKeySecret="sk-db2968be21644311a3ceca2de967552b";
        URL url = new URL("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
        HttpURLConnection connection=(HttpURLConnection)url.openConnection();

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Authorization","Bearer "+apiKeySecret);
        connection.setRequestProperty("Content-Type","application/json");
        connection.setDoOutput(true);


        String jsonInpuString = "{"
                + "\"model\": \"deepseek-r1-distill-qwen-1.5b\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为:"+diffcode+"\""
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
        return response.getChoices().get(0).getMessage().getContent();
    }
}
