package com.eduverse.aigc.agent;

import cn.hutool.core.lang.Assert;
import com.eduverse.aigc.enums.AgentTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RouteAgentTest {

    @Resource
    private RouteAgent routeAgent;

    @Test
    public void testChat(){
        Assert.equals(this.routeAgent.process("帮我推荐一些课", "1"), AgentTypeEnum.RECOMMEND.getAgentName());
        Assert.equals(this.routeAgent.process("下单购买这个课程", "1"), AgentTypeEnum.BUY.getAgentName());
        Assert.equals(this.routeAgent.process("java课程我能学会吗", "1"), AgentTypeEnum.CONSULT.getAgentName());
        Assert.equals(this.routeAgent.process("java是什么", "1"), AgentTypeEnum.KNOWLEDGE.getAgentName());
        System.out.println(this.routeAgent.process("帮我推荐一些课", "1"));// RECOMMEND
        System.out.println(this.routeAgent.process("下单购买这个课程", "1"));// BUY
        System.out.println(this.routeAgent.process("java课程我能学会吗", "1"));// CONSULT
        System.out.println(this.routeAgent.process("java是什么", "1"));// KNOWLEDGE
        System.out.println(this.routeAgent.process("你吃了吗？", "1"));// 自然语言返回
    }



}
