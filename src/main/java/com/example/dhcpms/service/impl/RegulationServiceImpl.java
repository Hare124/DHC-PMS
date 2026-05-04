package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Regulation;
import com.example.dhcpms.mapper.RegulationMapper;
import com.example.dhcpms.service.RegulationService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 物业制度服务实现类
 */
@Service
public class RegulationServiceImpl extends ServiceImpl<RegulationMapper, Regulation> implements RegulationService {

    @Override
    public IPage<Regulation> getRegulationList(Page<Regulation> page, String keyword, String type, String status) {
        LambdaQueryWrapper<Regulation> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索（制度名称、编号、内容）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Regulation::getRegulationName, keyword)
                    .or()
                    .like(Regulation::getRegulationNo, keyword)
                    .or()
                    .like(Regulation::getContent, keyword));
        }

        // 按类型筛选
        if (StringUtils.hasText(type)) {
            queryWrapper.eq(Regulation::getRegulationType, type);
        }

        // 按状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Regulation::getStatus, status);
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Regulation::getCreateTime);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Regulation createNewVersion(Long regulationId, Regulation newRegulation) {
        // 查询原制度
        Regulation oldRegulation = baseMapper.selectById(regulationId);
        if (oldRegulation == null) {
            return null;
        }

        // 设置新版本信息
        newRegulation.setParentRegulationId(oldRegulation.getParentRegulationId() != null ?
                oldRegulation.getParentRegulationId() : regulationId);
        newRegulation.setVersion(oldRegulation.getVersion() + 1);
        newRegulation.setStatus("draft");
        newRegulation.setCreateTime(LocalDateTime.now());

        // 保存新版本
        baseMapper.insert(newRegulation);

        // 可选：将旧版本标记为已作废
        // oldRegulation.setStatus("invalid");
        // baseMapper.updateById(oldRegulation);

        return newRegulation;
    }
}
