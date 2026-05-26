package com.eduverse.promotion.strategy.scope;

import com.eduverse.api.dto.promotion.OrderCourseDTO;
import com.eduverse.promotion.constants.ScopeType;

import java.util.List;

public interface Scope {

    boolean canUse(OrderCourseDTO course);

    ScopeType getType();

    List<Long> getScopeIds();
}
