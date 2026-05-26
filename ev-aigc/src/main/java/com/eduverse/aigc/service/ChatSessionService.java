package com.eduverse.aigc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduverse.aigc.entity.ChatSession;
import com.eduverse.aigc.vo.ChatSessionVO;
import com.eduverse.aigc.vo.MessageVO;
import com.eduverse.aigc.vo.SessionVO;

import java.util.List;
import java.util.Map;

public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 创建会话session
     * @param num 热门问题的数量
     * @return 会话信息
     */
    SessionVO createSession(Integer num);

    /**
     * 获取热门问题
     * @param num 获取的个数
     * @return 热门问题
     */
    List<SessionVO.Example> hotExamples(Integer num);


    /**
     * 根据会话id查询单个会话详情
     * @param sessionId 会话id
     * @return 消息列表
     */
    List<MessageVO> queryBySessionId(String sessionId);

    /**
     * 更新会话
     *
     * @param sessionId 会话ID，用于标识特定的聊天会话
     * @param title     新的会话标题，如果为空则不进行更新
     * @param userId    用户ID
     */
    void update(String sessionId, String title, Long userId);

    /**
     * 查询历史会话
     * @return
     */
    Map<String, List<ChatSessionVO>> queryHistorySession();

    /**
     * 删除历史会话
     * @param sessionId
     */
    void deleteHistorySession(String sessionId);

    /**
     * 更新历史会话标题
     * @param sessionId
     * @param title
     */
    void updateHistorySessionTitle(String sessionId, String title);
}