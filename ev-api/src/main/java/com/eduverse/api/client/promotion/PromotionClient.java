package com.eduverse.api.client.promotion;

import com.eduverse.api.client.promotion.fallback.PromotionClientFallback;
import com.eduverse.api.dto.promotion.CouponDiscountDTO;
import com.eduverse.api.dto.promotion.OrderCouponDTO;
import com.eduverse.api.dto.promotion.OrderCourseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "promotion-service", fallbackFactory = PromotionClientFallback.class)
public interface PromotionClient {

    @PostMapping("/user-coupons/available")
    List<CouponDiscountDTO> findDiscountSolution(@RequestBody List<OrderCourseDTO> orderCourses);

    @PostMapping("/user-coupons/discount")
    CouponDiscountDTO queryDiscountDetailByOrder(@RequestBody OrderCouponDTO orderCouponDTO);

    @PutMapping("/user-coupons/use")
    void writeOffCoupon(@RequestParam("couponIds") List<Long> userCouponIds);

    @PutMapping("/user-coupons/refund")
    void refundCoupon(@RequestParam("couponIds") List<Long> userCouponIds);

    @GetMapping("/user-coupons/rules")
    List<String> queryDiscountRules(@RequestParam("couponIds") List<Long> userCouponIds);
}
