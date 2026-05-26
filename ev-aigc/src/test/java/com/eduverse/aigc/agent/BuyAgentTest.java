package com.eduverse.aigc.agent;

import com.eduverse.aigc.vo.ChatEventVO;
import com.eduverse.common.utils.UserContext;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
class BuyAgentTest {

    @Resource
    private BuyAgent buyAgent;

    @Test
    public void processStream() throws InterruptedException {
        String question = "下单购买，课程id为：1589905661084430337";
        String sessionId = "123";
        UserContext.setUser(2L);
        Flux<ChatEventVO> flux = buyAgent.processStream(question, sessionId);
        flux.subscribe(System.out::println);

        // 阻塞主线程，防止主线程结束，子线程终止
        Thread.sleep(100000);
    }

}