package com.eduverse.aigc.tools;

import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.eduverse.aigc.config.ToolResultHolder;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.tools.result.PrePlaceOrder;
import com.eduverse.api.client.trade.TradeClient;
import com.eduverse.common.utils.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderTools {

    private final TradeClient tradeClient;


    // 此工具是大模型调用的，所以是一个新的线程，大模型无法得知之前的token，
    // 所以需要手动设置用户id到ThreadLocal中，这样大模型调用交易微服务时，才不会因为身份验证而失败
    @Tool(description = Constant.Tools.PRE_PLACE_ORDER)
    public PrePlaceOrder prePlaceOrder(
            // 课程Id定义的是Long类型，但是这里用Number，是因为课程ID有可能创建的时候为“123”这种
            // 大模型会默认将其作为int类型传参所以这里用Number防止报错
            @ToolParam(description = Constant.ToolParams.COURSE_IDS) List<Number> courseIds,
            ToolContext toolContext
    ) {
        // 设置用户ID，用于身份验证，否在在Feign调用时会出现401错误
        UserContext.setUser(Convert.toLong(toolContext.getContext().get(Constant.USER_ID)));
        // 大模型传入的ids，可能是int类型，所以转化为long类型，再调用Feign
        var orderConfirmVO = this.tradeClient.prePlaceOrder(CollStreamUtil.toList(courseIds, Number::longValue));

        return Optional.ofNullable(orderConfirmVO)
                .map(PrePlaceOrder::of)
                .map(prePlaceOrder -> {
                    // 大key
                    String requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
                    // 小key
                    String field = StrUtil.lowerFirst(prePlaceOrder.getClass().getSimpleName());
                    ToolResultHolder.put(requestId, field, prePlaceOrder);
                    return prePlaceOrder;
                })
                .orElse(null);
    }
}