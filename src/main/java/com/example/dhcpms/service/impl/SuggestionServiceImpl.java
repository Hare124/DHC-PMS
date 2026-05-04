package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.dto.SuggestionReplyDTO;
import com.example.dhcpms.dto.SuggestionStatusDTO;
import com.example.dhcpms.dto.SuggestionSubmitDTO;
import com.example.dhcpms.entity.Suggestion;
import com.example.dhcpms.mapper.SuggestionMapper;
import com.example.dhcpms.service.SuggestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 意见反馈服务实现类
 */
@Slf4j
@Service
public class SuggestionServiceImpl extends ServiceImpl<SuggestionMapper, Suggestion>
    implements SuggestionService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long submitSuggestion(SuggestionSubmitDTO dto) {
        try {
            log.info("收到意见反馈提交请求：{}", dto);

            // 创建意见反馈实体
            Suggestion suggestion = new Suggestion();
            suggestion.setUserId(dto.getUserId());
            suggestion.setOwnerId(dto.getOwnerId());
            suggestion.setType(dto.getType());
            suggestion.setTitle(dto.getTitle());
            suggestion.setContent(dto.getContent());
            suggestion.setImages(dto.getImages());
            suggestion.setContactPhone(dto.getContactPhone());
            suggestion.setStatus("pending");  // 默认待处理
            suggestion.setCreateTime(LocalDateTime.now());
            suggestion.setUpdateTime(LocalDateTime.now());

            // 保存到数据库
            boolean success = this.save(suggestion);

            if (success) {
                log.info("意见反馈提交成功，ID: {}", suggestion.getId());
                return suggestion.getId();
            } else {
                log.error("意见反馈提交失败");
                throw new RuntimeException("意见反馈提交失败");
            }
        } catch (Exception e) {
            log.error("意见反馈提交异常", e);
            throw new RuntimeException("意见反馈提交异常：" + e.getMessage());
        }
    }

    @Override
    public List<Suggestion> getOwnerSuggestionList(Long userId) {
        try {
            log.info("查询用户意见反馈列表，userId: {}", userId);

            LambdaQueryWrapper<Suggestion> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Suggestion::getUserId, userId)
                       .orderByDesc(Suggestion::getCreateTime);

            List<Suggestion> list = this.list(queryWrapper);
            log.info("查询到 {} 条意见反馈记录", list.size());
            return list;
        } catch (Exception e) {
            log.error("查询意见反馈列表异常", e);
            throw new RuntimeException("查询意见反馈列表异常：" + e.getMessage());
        }
    }

    @Override
    public Suggestion getSuggestionDetail(Long id) {
        try {
            log.info("查询意见反馈详情，id: {}", id);

            Suggestion suggestion = this.getById(id);
            if (suggestion == null) {
                log.warn("意见反馈不存在，id: {}", id);
                throw new RuntimeException("意见反馈不存在");
            }

            return suggestion;
        } catch (Exception e) {
            log.error("查询意见反馈详情异常", e);
            throw new RuntimeException("查询意见反馈详情异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replySuggestion(SuggestionReplyDTO dto) {
        try {
            log.info("收到意见反馈回复请求，id: {}, 回复人 ID: {}", dto.getId(), dto.getReplyUserId());

            // 查询意见反馈
            Suggestion suggestion = this.getById(dto.getId());
            if (suggestion == null) {
                log.warn("意见反馈不存在，id: {}", dto.getId());
                throw new RuntimeException("意见反馈不存在");
            }

            // 更新回复信息
            suggestion.setReplyContent(dto.getReplyContent());
            suggestion.setReplyUserId(dto.getReplyUserId());
            suggestion.setReplyTime(LocalDateTime.now());
            suggestion.setStatus("replied");  // 设置为已回复
            suggestion.setUpdateTime(LocalDateTime.now());

            boolean success = this.updateById(suggestion);

            if (success) {
                log.info("意见反馈回复成功，id: {}", dto.getId());
            } else {
                log.error("意见反馈回复失败，id: {}", dto.getId());
                throw new RuntimeException("意见反馈回复失败");
            }
        } catch (Exception e) {
            log.error("意见反馈回复异常", e);
            throw new RuntimeException("意见反馈回复异常：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSuggestionStatus(SuggestionStatusDTO dto) {
        try {
            log.info("收到意见反馈状态更新请求，id: {}, status: {}", dto.getId(), dto.getStatus());

            // 查询意见反馈
            Suggestion suggestion = this.getById(dto.getId());
            if (suggestion == null) {
                log.warn("意见反馈不存在，id: {}", dto.getId());
                throw new RuntimeException("意见反馈不存在");
            }

            // 更新状态
            suggestion.setStatus(dto.getStatus());
            suggestion.setUpdateTime(LocalDateTime.now());

            boolean success = this.updateById(suggestion);

            if (success) {
                log.info("意见反馈状态更新成功，id: {}", dto.getId());
            } else {
                log.error("意见反馈状态更新失败，id: {}", dto.getId());
                throw new RuntimeException("意见反馈状态更新失败");
            }
        } catch (Exception e) {
            log.error("意见反馈状态更新异常", e);
            throw new RuntimeException("意见反馈状态更新异常：" + e.getMessage());
        }
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<Suggestion> getSuggestionList(
        Page<Suggestion> page, String type, String status) {
        try {
            log.info("分页查询意见反馈列表，type: {}, status: {}", type, status);

            LambdaQueryWrapper<Suggestion> queryWrapper = new LambdaQueryWrapper<>();

            // 条件查询
            if (type != null && !type.isEmpty()) {
                queryWrapper.eq(Suggestion::getType, type);
            }
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq(Suggestion::getStatus, status);
            }

            // 按创建时间倒序
            queryWrapper.orderByDesc(Suggestion::getCreateTime);

            com.baomidou.mybatisplus.core.metadata.IPage<Suggestion> result =
                this.page(page, queryWrapper);

            log.info("查询到 {} 条意见反馈记录", result.getTotal());
            return result;
        } catch (Exception e) {
            log.error("分页查询意见反馈列表异常", e);
            throw new RuntimeException("分页查询意见反馈列表异常：" + e.getMessage());
        }
    }
}
