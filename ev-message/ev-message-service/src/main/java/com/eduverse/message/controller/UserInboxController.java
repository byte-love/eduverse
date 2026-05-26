package com.eduverse.message.controller;


import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.message.domain.dto.UserInboxDTO;
import com.eduverse.message.domain.dto.UserInboxFormDTO;
import com.eduverse.message.domain.query.UserInboxQuery;
import com.eduverse.message.service.IUserInboxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户通知记录 前端控制器
 * </p>
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/inboxes")
@Tag(name = "用户收件箱接口")
public class UserInboxController {

    private final IUserInboxService inboxService;

    @PostMapping
    @Operation(summary = "发送私信")
    public Long sentMessageToUser(@RequestBody UserInboxFormDTO userInboxFormDTO){
        return inboxService.sentMessageToUser(userInboxFormDTO);
    }

    @Operation(summary = "分页查询收件箱")
    @GetMapping
    public PageDTO<UserInboxDTO> queryUserInBoxesPage(UserInboxQuery query){
        return inboxService.queryUserInBoxesPage(query);
    }
}