package com.eduverse.aigc.service.impl;

import com.eduverse.aigc.config.SystemPromptConfig;
import com.eduverse.aigc.service.ChatTextService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class ChatTextServiceImpl implements ChatTextService {

    private final ChatClient openAiChatClient;

    private final SystemPromptConfig systemPromptConfig;


    /**
     * 文本问答
     *
     * @param question
     * @return
     */
    @Override
    public String chatText(String question) {
        return this.openAiChatClient.prompt()
                .system(promptSystem -> promptSystem.text(this.systemPromptConfig.getTextSystemMessage().get()))
                .user(question)
                .call()
                .content();
    }



}
