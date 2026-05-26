package com.eduverse.learning.controller;

import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.learning.domain.query.QuestionAdminPageQuery;
import com.eduverse.learning.domain.vo.QuestionAdminVO;
import com.eduverse.learning.service.IInteractionQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 互动提问的问题表 前端控制器
 */
@RestController
@RequestMapping("/admin/questions")
@Tag(name = "互动问答的相关接口")
@RequiredArgsConstructor
public class InteractionQuestionAdminController {

    private final IInteractionQuestionService questionService;

    @Operation(summary = "管理端分页查询互动问题")
    @GetMapping("page")
    public PageDTO<QuestionAdminVO> queryQuestionPageAdmin(QuestionAdminPageQuery query) {
        return questionService.queryQuestionPageAdmin(query);
    }

    @Operation(summary = "管理端根据id查询互动问题")
    @GetMapping("{id}")
    public QuestionAdminVO queryQuestionByIdAdmin(@PathVariable("id") Long id) {
        return questionService.queryQuestionByIdAdmin(id);
    }

    @Operation(summary = "隐藏或显示问题")
    @PutMapping("/{id}/hidden/{hidden}")
    public void hiddenQuestion(
            @Parameter(description = "问题id", example = "1") @PathVariable("id") Long id,
            @Parameter(description = "是否隐藏，true/false", example = "true") @PathVariable("hidden") Boolean hidden
    ) {
        questionService.hiddenQuestion(id, hidden);
    }
}
