package com.eduverse.aigc.controller;

import com.eduverse.aigc.service.ChatSessionService;
import com.eduverse.aigc.vo.ChatSessionVO;
import com.eduverse.aigc.vo.MessageVO;
import com.eduverse.aigc.vo.SessionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final ChatSessionService chatSessionService;

    /**
     * 新建会话
     */
    @PostMapping
    public SessionVO createSession(@RequestParam(value = "n", defaultValue = "3") Integer num) {
        return this.chatSessionService.createSession(num);
    }

    /**
     * 获取热门会话
     * @return 热门会话列表
     */
    @GetMapping("/hot")
    public List<SessionVO.Example> hotExamples(@RequestParam(value = "n", defaultValue = "3") Integer num) {
        return this.chatSessionService.hotExamples(num);
    }

    /**
     * 根据会话id查询单个会话详情
     * @param sessionId 会话id
     * @return 聊天消息列表
     */
    @GetMapping("/{sessionId}")
    public List<MessageVO> queryBySessionId(@PathVariable String sessionId) {
        return this.chatSessionService.queryBySessionId(sessionId);
    }

    /**
     * 查询历史会话列表
     */
    @GetMapping("/history")
    public Map<String, List<ChatSessionVO>> queryHistorySession() {
        return this.chatSessionService.queryHistorySession();
    }

    /**
     * 删除会话
     * @param sessionId 会话id
     */
    @DeleteMapping("/history")
    public void deleteHistorySession(String sessionId) {
        this.chatSessionService.deleteHistorySession(sessionId);
    }

    /**
     * 修改会话标题
     * @param sessionId 会话id
     * @param title 标题
     */
    @PutMapping("/history")
    public void update(@RequestParam("sessionId") String sessionId,
                       @RequestParam("title") String title) {
        this.chatSessionService.updateHistorySessionTitle(sessionId, title);
    }




}