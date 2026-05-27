package com.eduverse.aigc.tools;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.eduverse.aigc.config.ToolResultHolder;
import com.eduverse.aigc.constants.Constant;
import com.eduverse.aigc.tools.result.CourseInfo;
import com.eduverse.api.client.course.CourseClient;
import com.eduverse.api.dto.course.CourseBaseInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseClient courseClient;

    private static final String FIELD_FORMAT = "{}_{}";


    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(@ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId,
                                      ToolContext toolContext) {
        return Optional
                .ofNullable(courseId)
                .map(id -> queryCourseInternal(id, Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID))))
                .orElse(null);
    }

    @Tool(description = Constant.Tools.QUERY_COURSES_BY_IDS)
    public List<CourseInfo> queryCoursesByIds(@ToolParam(description = Constant.ToolParams.COURSE_IDS) List<Long> courseIds,
                                              ToolContext toolContext) {
        return queryCoursesByIdsInternal(courseIds, Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID)));
    }

    /**
     * 批量查询课程详情并存入ToolResultHolder，可被工具方法和程序化调用。
     */
    public List<CourseInfo> queryCoursesByIdsInternal(List<Long> courseIds, String requestId) {
        if (courseIds == null || courseIds.isEmpty()) {
            return List.of();
        }
        return courseIds.stream()
                .map(id -> queryCourseInternal(id, requestId))
                .filter(Objects::nonNull)
                .toList();
    }

    private CourseInfo queryCourseInternal(Long courseId, String requestId) {
        CourseInfo courseInfo = CourseInfo.of(courseClient.baseInfo(courseId, true));
        if (courseInfo != null) {
            String field = StrUtil.format(FIELD_FORMAT,
                    StrUtil.lowerFirst(CourseInfo.class.getSimpleName()),
                    courseInfo.getId());
            ToolResultHolder.put(requestId, field, courseInfo);
        }
        return courseInfo;
    }

    /**
     * 获取所有已发布课程的基础信息（不含价格），供程序内部使用，不作为LLM工具。
     */
    public List<CourseInfo> getAvailableCourses() {
        var courses = courseClient.getPublishedCourseBaseInfos();
        if (courses == null || courses.isEmpty()) {
            return List.of();
        }
        return courses.stream()
                .map(dto -> CourseInfo.builder()
                        .id(dto.getId())
                        .name(dto.getName())
                        .validDuration(dto.getValidDuration())
                        .usePeople(dto.getUsePeople())
                        .build())
                .toList();
    }

}
