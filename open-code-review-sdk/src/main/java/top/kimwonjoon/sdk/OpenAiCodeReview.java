package top.kimwonjoon.sdk;


import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import top.kimwonjoon.sdk.type.uitls.CallWithMessageService;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class OpenAiCodeReview {


    public static void main(String[] args) throws IOException, InterruptedException {
        CallWithMessageService service = new CallWithMessageService();
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
        System.out.println("ExitCode:"+diffCode);
        System.out.println("评审代码"+diffCode.toString());

        //2.通过 deepseek 代码评审
        try {
            GenerationResult result = service.callWithMessage(diffCode.toString());
            System.out.println("思考过程：");
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getReasoningContent());
            System.out.println("回复内容：");
            System.out.println(result.getOutput().getChoices().get(0).getMessage().getContent());
        } catch (ApiException | NoApiKeyException | InputRequiredException e) {
            // 使用日志框架记录异常信息
            System.err.println("An error occurred while calling the generation service: " + e.getMessage());
        }
        System.exit(0);

    }
}
