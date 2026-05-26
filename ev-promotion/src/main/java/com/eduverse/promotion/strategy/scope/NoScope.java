package com.eduverse.promotion.strategy.scope;

import com.eduverse.api.dto.promotion.OrderCourseDTO;
import com.eduverse.promotion.constants.ScopeType;

import java.util.List;

public class NoScope implements Scope{

    @Override
    public boolean canUse(OrderCourseDTO course) {
        return true;
    }

    @Override
    public ScopeType getType() {
        return ScopeType.ALL;
    }

    @Override
    public List<Long> getScopeIds() {
        return null;
    }

}
