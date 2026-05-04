package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.User;

/**
 * User 表的 Service 接口
 */
public interface UserService extends IService<User> {
    User findByUsername(String username);
    User findByPhone(String phone);
    boolean existsByPhone(String phone);
    boolean existsByUsername(String username);
    String login(String username, String password);
    boolean isManager(Long userId);
    boolean isOwner(Long userId);
    // 根据ID查询用户名
    String getUserNameById(Long userId);

}

