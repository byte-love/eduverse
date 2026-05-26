package com.eduverse.promotion.service.impl;

import com.eduverse.promotion.domain.po.Promotion;
import com.eduverse.promotion.mapper.PromotionMapper;
import com.eduverse.promotion.service.IPromotionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 促销活动，形式多种多样，例如：优惠券 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-09-06
 */
@Service
public class PromotionServiceImpl extends ServiceImpl<PromotionMapper, Promotion> implements IPromotionService {

}
