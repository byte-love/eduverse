package com.eduverse.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.eduverse.api.dto.user.LoginFormDTO;
import com.eduverse.api.dto.user.UserDTO;
import com.eduverse.common.domain.dto.LoginUserDTO;
import com.eduverse.user.domain.dto.UserFormDTO;
import com.eduverse.user.domain.po.User;
import com.eduverse.user.domain.vo.UserDetailVO;

/**
 * <p>
 * 学员用户表 服务类
 * </p>
 *
 * @author 虎哥
 * @since 2022-06-28
 */
public interface IUserService extends IService<User> {
    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    void resetPassword(Long userId);

    UserDetailVO myInfo();

    void addUserByPhone(User user, String code);

    void updatePasswordByPhone(String cellPhone, String code, String password);

    Long saveUser(UserDTO userDTO);

    void updateUser(UserDTO userDTO);

    void updateUserWithPassword(UserFormDTO userDTO);
}
