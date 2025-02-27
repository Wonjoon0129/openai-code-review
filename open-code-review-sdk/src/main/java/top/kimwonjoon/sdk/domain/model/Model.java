package top.kimwonjoon.sdk.domain.model;

public enum Model {

    @Deprecated
    CHATGLM_6B_SSE("chatGLM_6b_SSE", "ChatGLM-6B 测试模型"),
    @Deprecated
    CHATGLM_LITE("chatglm_lite", "轻量版模型，适用对推理速度和成本敏感的场景"),
    @Deprecated
    CHATGLM_LITE_32K("chatglm_lite_32k", "标准版模型，适用兼顾效果和成本的场景"),
    @Deprecated
    CHATGLM_STD("chatglm_std", "适用于对知识量、推理能力、创造力要求较高的场景"),
    @Deprecated
    CHATGLM_PRO("chatglm_pro", "适用于对知识量、推理能力、创造力要求较高的场景"),
    /** 智谱AI 23年06月发布 */
    CHATGLM_TURBO("chatglm_turbo", "适用于对知识量、推理能力、创造力要求较高的场景"),
    /** 智谱AI 24年01月发布 */
    GLM_3_5_TURBO("glm-3-turbo","适用于对知识量、推理能力、创造力要求较高的场景"),
    GLM_4("glm-4","适用于复杂的对话交互和深度内容创作设计的场景"),
    DEEPSEEK_V3("deepseek-v3","MoE 模型，671B 参数，激活 37B，在 14.8T Token 上进行了预训练，在长文本、代码、数学、百科、中文能力上表现优秀"),
    DEEPSEEK_R1_DISTILL_QWEN_1_5B("deepseek-r1-distill-qwen-1.5b","在视觉理解、音频理解等能力上，Qwen-Omni 模型也表现出色"),
    COGVIEW_3("cogview-3","根据用户的文字描述生成图像,使用同步调用方式请求接口"),

    ;
    private final String code;
    private final String info;

    Model(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }

}
