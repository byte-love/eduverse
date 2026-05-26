package com.eduverse.promotion.service;

import com.eduverse.api.dto.promotion.CouponDiscountDTO;
import com.eduverse.api.dto.promotion.OrderCouponDTO;
import com.eduverse.api.dto.promotion.OrderCourseDTO;

import java.util.List;

public interface IDiscountService {
    List<CouponDiscountDTO> findDiscountSolution(List<OrderCourseDTO> orderCourses);

    CouponDiscountDTO queryDiscountDetailByOrder(OrderCouponDTO orderCouponDTO);
}
