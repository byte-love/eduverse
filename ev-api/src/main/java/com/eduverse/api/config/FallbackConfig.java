package com.eduverse.api.config;

import com.eduverse.api.client.learning.fallback.LearningClientFallback;
import com.eduverse.api.client.promotion.fallback.PromotionClientFallback;
import com.eduverse.api.client.remark.fallback.RemarkClientFallback;
import com.eduverse.api.client.trade.fallback.TradeClientFallback;
import com.eduverse.api.client.user.fallback.UserClientFallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FallbackConfig {
    @Bean
    public LearningClientFallback learningClientFallback(){
        return new LearningClientFallback();
    }

    @Bean
    public TradeClientFallback tradeClientFallback(){
        return new TradeClientFallback();
    }

    @Bean
    public UserClientFallback userClientFallback(){
        return new UserClientFallback();
    }

    @Bean
    public RemarkClientFallback remarkClientFallback(){
        return new RemarkClientFallback();
    }

    @Bean
    public PromotionClientFallback promotionClientFallback(){
        return new PromotionClientFallback();
    }
}
