package com.eduverse.user.service;

import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.user.domain.query.UserPageQuery;
import com.eduverse.user.domain.vo.TeacherPageVO;

/**
 * <p>
 * 教师详情表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-12
 */
public interface ITeacherService{
    PageDTO<TeacherPageVO> queryTeacherPage(UserPageQuery pageQuery);

}
