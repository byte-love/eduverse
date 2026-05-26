package com.eduverse.learning.service;

import com.eduverse.learning.domain.po.InteractionQuestion;

public interface AIService {

    /**
     * AI 自动回复
     *
     * @param interactionQuestion 问题对象
     */
    void autoReply(InteractionQuestion interactionQuestion);

}