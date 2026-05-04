package com.example.dhcpms.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.dto.SuggestionReplyDTO;
import com.example.dhcpms.dto.SuggestionStatusDTO;
import com.example.dhcpms.dto.SuggestionSubmitDTO;
import com.example.dhcpms.entity.Suggestion;

import java.util.List;

/**
 * 意见反馈服务接口
 */
public interface SuggestionService extends IService<Suggestion> {

    /**
     * 提交意见反馈
     * @param dto 意见反馈数据
     * @return 意见反馈 ID
     */
    Long submitSuggestion(SuggestionSubmitDTO dto);

    /**
     * 获取业主意见反馈列表
     * @param userId 用户 ID
     * @return 意见反馈列表
     */
    List<Suggestion> getOwnerSuggestionList(Long userId);

    /**
     * 获取意见反馈详情
     * @param id 意见反馈 ID
     * @return 意见反馈详情
     */
    Suggestion getSuggestionDetail(Long id);

    /**
     * 物业回复意见反馈
     * @param dto 回复数据
     */
    void replySuggestion(SuggestionReplyDTO dto);

    /**
     * 更新意见反馈状态
     * @param dto 状态数据
     */
    void updateSuggestionStatus(SuggestionStatusDTO dto);

    /**
     * 分页查询意见反馈列表（物业端）
     * @param page 分页参数
     * @param type 意见类型
     * @param status 处理状态
     * @return 分页结果
     */
    com.baomidou.mybatisplus.core.metadata.IPage<Suggestion> getSuggestionList(
        Page<Suggestion> page, String type, String status);
}
