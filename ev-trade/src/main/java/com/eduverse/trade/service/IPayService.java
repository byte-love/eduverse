package com.eduverse.trade.service;

import com.eduverse.trade.domain.dto.OrderDelayQueryDTO;
import com.eduverse.trade.domain.dto.PayApplyFormDTO;
import com.eduverse.trade.domain.vo.PayChannelVO;

import java.util.List;

public interface IPayService {
    List<PayChannelVO> queryPayChannels();

    String applyPayOrder(PayApplyFormDTO payApply);

    void queryPayResult(OrderDelayQueryDTO message);
}
