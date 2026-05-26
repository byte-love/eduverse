package com.eduverse.aigc.controller;

import com.eduverse.aigc.service.ChatService;
import com.eduverse.aigc.dto.ChatDTO;
import com.eduverse.aigc.service.ChatTextService;
import com.eduverse.aigc.vo.ChatEventVO;
import com.eduverse.aigc.vo.TemplateVO;
import com.eduverse.common.annotations.NoWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    private final ChatTextService chatTextService;

    private static final TemplateVO TEMPLATE_VO = new TemplateVO();

    /**
     * 流式对话
     * @param chatDTO
     * @return
     */
    // 该注解将告诉Spring MVC，该方法返回的是一个流，不需要进行包装。
    @NoWrapper
    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEventVO> chat(@RequestBody ChatDTO chatDTO) {
        return this.chatService.chat(chatDTO.getQuestion(), chatDTO.getSessionId());
    }

    /**
     * 停止对话
     * @param sessionId
     */
    @PostMapping("/stop")
    public void stop(@RequestParam String sessionId) {
        this.chatService.stop(sessionId);
    }

    @PostMapping("/text")
    public String chatText(@RequestBody String question) {
        return this.chatTextService.chatText(question);
    }


    @GetMapping("/templates")
    public TemplateVO getTemplates() {
        return TEMPLATE_VO;
    }
}