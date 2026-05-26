package com.eduverse.aigc.agent;

import cn.hutool.core.map.MapUtil;
import com.alibaba.dashscope.app.Application;
import com.alibaba.dashscope.app.ApplicationParam;
import com.alibaba.dashscope.app.ApplicationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import io.reactivex.Flowable;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AppTest {

    @Test
    public void testAppCall() throws Exception {
        // 构造业务参数
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VyIjp7InVzZXJJZCI6Miwicm9sZUlkIjoyLCJyZW1lbWJlck1lIjpmYWxzZX0sImV4cCI6MTc3ODM3MjIwOX0.dmf_5rICnOISdDAAtd0DLvL2SvrKh4mV_U66lmP5SnmuGLBQyhDe1a8p9QagzHbRe4qNxdFCevDuRG8pZfZSjzggVZHI6tSNDO-pYve0AlbyijW2k21kffYdW8cuhCrUd5KFm6fN1fR3xkP37Yt29c6YQ_5zXzRO6RuuHb6K8-8MRmQP8JOb3r0CCL_pd3oi7jNORMzTBtSTXrEyd2metT1jreWRYC3MZlsuEJvhKOKrtDx08rBpBEQ5Kk98Pb-Ak9_AgL1qSmuvh_7JlBtm3SdG3jLpf5RAihRdUGFo7Bromheibs6AX4jkgF08u8loT4VWhmGnIoIbyxTaAzCU5Q";
        Map<String, Object> bizParams = MapUtil.<String, Object>builder()
                .put("user_defined_tokens", MapUtil.of("tool_876a6142-6ab1-4c5c-9487-c939358b2021", // 工具id
                        MapUtil.of("user_token", token)))
                .build();

        // bizParams.add("user_defined_tokens", JsonObject);
        ApplicationParam param = ApplicationParam.builder()
                // 若没有配置环境变量，可用百炼API Key将下行替换为：.apiKey("sk-xxx")。但不建议在生产环境中直接将API Key硬编码到代码中，以减少API Key泄露风险。
                .apiKey(System.getenv("Ali-AI-API-KEY"))
                .appId("a7907f7073024e3a8971df1e30a96976") // 智能体id
                .prompt("查询课程，id为：1880533253575225346")
                .incrementalOutput(true) // 开启增量输出
                .bizParams(JsonUtils.toJsonObject(bizParams))
                .build();

        Application application = new Application();
        Flowable<ApplicationResult> result = application.streamCall(param);

        // 阻塞式的打印内容
        result.blockingForEach(data -> {
            System.out.printf("%s\n",data.getOutput().getText());
        });

    }

}