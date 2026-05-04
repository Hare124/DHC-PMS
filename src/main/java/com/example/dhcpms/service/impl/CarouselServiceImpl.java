package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.Carousel;
import com.example.dhcpms.mapper.CarouselMapper;
import com.example.dhcpms.service.CarouselService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 轮播图服务实现类
 */
@Service
public class CarouselServiceImpl extends ServiceImpl<CarouselMapper, Carousel> implements CarouselService {

    @Override
    public IPage<Carousel> getCarouselList(Page<Carousel> page, String keyword, String status) {
        LambdaQueryWrapper<Carousel> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词模糊搜索（标题）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Carousel::getTitle, keyword);
        }

        // 按状态筛选
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(Carousel::getStatus, status);
        }

        // 按排序顺序升序
        queryWrapper.orderByAsc(Carousel::getSortOrder);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<Carousel> getActiveCarousels() {
        LambdaQueryWrapper<Carousel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Carousel::getStatus, "1")
                .orderByAsc(Carousel::getSortOrder);
        return baseMapper.selectList(queryWrapper);
    }
}
