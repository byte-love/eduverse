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

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CourseTools {

    private final CourseClient courseClient;

    private static final String FIELD_FORMAT = "{}_{}";


    // 方法名不能乱写，要跟提示词里面给大模型的方法名一致
    @Tool(description = Constant.Tools.QUERY_COURSE_BY_ID)
    public CourseInfo queryCourseById(@ToolParam(description = Constant.ToolParams.COURSE_ID) Long courseId,
                                      ToolContext toolContext) {
//        if (courseId != null) {
//            CourseBaseInfoDTO courseBaseInfoDTO = courseClient.baseInfo(courseId, true);
//            return CourseInfo.of(courseBaseInfoDTO);
//        } else {
//            return null;
//        }
          return Optional
                  .ofNullable(courseId)
                      // 获取课程信息
                      .map(id -> CourseInfo.of(courseClient.baseInfo(id, true)))
                      // 存储课程卡片到容器中
                      .map(courseInfo -> {
                          // 大key：requestId
                          String requestId = Convert.toStr(toolContext.getContext().get(Constant.REQUEST_ID));
                          // 小key：courseInfo_课程Id
                          String field = StrUtil.format(FIELD_FORMAT,
                                  StrUtil.lowerFirst(CourseInfo.class.getSimpleName()),
                                  courseId);
                          // 准备value：courseInfo对象
                          // 存入容器
                          ToolResultHolder.put(requestId, field, courseInfo);
                          return courseInfo;
                      })
                  .orElse(null);
    }

}
