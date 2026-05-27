package com.eduverse.aigc.constants;

public interface Constant {

    String REQUEST_ID = "requestId";
    String USER_ID = "userId";
    String STOP = "STOP";
    String ID = "id";

    interface Tools {
        String QUERY_COURSE_BY_ID = "根据课程id查询课程详细信息";
        String QUERY_COURSES_BY_IDS = "根据课程id列表批量查询课程详细信息，返回存在的课程列表";
        String GET_AVAILABLE_COURSES = "获取所有可选的已发布课程列表，仅包含课程id、名称、适用人群，不包含价格和详细介绍。如需价格和详情必须调用queryCoursesByIds";
        String PRE_PLACE_ORDER = "购买课程预下单操作";
    }

    interface ToolParams {
        String COURSE_ID = "课程id";
        String COURSE_IDS = "课程id列表";
    }

}