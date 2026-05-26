package com.eduverse.promotion.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.promotion.domain.po.Coupon;
import com.eduverse.promotion.domain.po.ExchangeCode;
import com.eduverse.promotion.domain.query.CodeQuery;
import com.eduverse.promotion.domain.vo.ExchangeCodeVO;

/**
 * <p>
 * 兑换码 服务类
 * </p>
 *
 * @author 虎哥
 */
public interface IExchangeCodeService extends IService<ExchangeCode> {
    void asyncGenerateCode(Coupon coupon);

    boolean updateExchangeMark(long serialNum, boolean mark);

    PageDTO<ExchangeCodeVO> queryCodePage(CodeQuery query);

    Long exchangeTargetId(long serialNum);
}
