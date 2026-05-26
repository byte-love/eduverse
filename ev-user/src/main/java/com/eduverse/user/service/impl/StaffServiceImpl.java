package com.eduverse.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eduverse.api.cache.RoleCache;
import com.eduverse.common.domain.dto.PageDTO;
import com.eduverse.common.enums.UserType;
import com.eduverse.common.utils.BeanUtils;
import com.eduverse.user.domain.po.UserDetail;
import com.eduverse.user.domain.query.UserPageQuery;
import com.eduverse.user.domain.vo.StaffVO;
import com.eduverse.user.service.IStaffService;
import com.eduverse.user.service.IUserDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 员工详情表 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2022-07-12
 */
@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements IStaffService {

    private final IUserDetailService detailService;
    private final RoleCache roleCache;
    @Override
    public PageDTO<StaffVO> queryStaffPage(UserPageQuery query) {
        // 1.搜索
        Page<UserDetail> p = detailService.queryUserDetailByPage(query, UserType.STAFF);
        // 2.处理vo
        return PageDTO.of(p, u -> {
            StaffVO v = BeanUtils.toBean(u, StaffVO.class);
            v.setRoleName(roleCache.getRoleName(u.getRoleId()));
            return v;
        });
    }
}
