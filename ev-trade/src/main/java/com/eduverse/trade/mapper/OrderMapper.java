package com.eduverse.trade.mapper;

import com.eduverse.trade.domain.po.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 订单 Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2022-08-29
 */
public interface OrderMapper extends BaseMapper<Order> {

    Order getById(Long id);
}
