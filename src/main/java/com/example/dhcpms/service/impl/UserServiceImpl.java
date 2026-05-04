package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.User;
import com.example.dhcpms.mapper.UserMapper;
import com.example.dhcpms.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User findByUsername(String username) {
        return this.lambdaQuery().eq(User::getUsername, username).one();
    }
    @Override
    public User findByPhone(String phone) {
        // 核心：将查询字段从 User::getUsername 改为 User::getPhone
        return this.lambdaQuery().eq(User::getPhone, phone).one();
    }
    @Override
    public boolean existsByPhone(String phone) {
        return this.lambdaQuery().eq(User::getPhone, phone).exists();
    }

    @Override
    public boolean existsByUsername(String username) {
        return this.lambdaQuery().eq(User::getUsername, username).exists();
    }

    /**
     * 登录逻辑：验证用户名和密码，生成 JWT token
     */
    public String login(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            // 生成 JWT token
            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24小时有效
                    .signWith(SignatureAlgorithm.HS512, "your-secret-key")
                    .compact();
        }
        return null; // 登录失败
    }

    @Override
    public boolean isManager(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId)
                .eq(User::getRole, 1) // 1-物业人员
                .eq(User::getStatus, 1); // 正常状态
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean isOwner(Long userId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getId, userId)
                .eq(User::getRole, 2) // 2-业主
                .eq(User::getStatus, 1); // 正常状态
        return this.count(wrapper) > 0;
    }
    @Override
    public String getUserNameById(Long userId) {
        User user = this.getById(userId);
        return user != null ? user.getName() : "未知";
    }
}
