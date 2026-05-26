package com.eduverse.message.service;

import com.eduverse.api.dto.sms.SmsInfoDTO;
import com.eduverse.api.dto.user.UserDTO;
import com.eduverse.message.domain.po.NoticeTemplate;

import java.util.List;

public interface ISmsService {
    void sendMessageByTemplate(NoticeTemplate noticeTemplate, List<UserDTO> users);

    void sendMessage(SmsInfoDTO smsInfoDTO);

    void sendMessageAsync(SmsInfoDTO smsInfoDTO);
}
