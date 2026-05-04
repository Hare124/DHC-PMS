package com.example.dhcpms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.dhcpms.entity.Carousel;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface CarouselService extends IService<Carousel> {

    /**
     * 分页查询轮播图列表
     */
    IPage<Carousel> getCarouselList(Page<Carousel> page, String keyword, String status);

    /**
     * 查询启用的轮播图（按排序顺序）
     */
    List<Carousel> getActiveCarousels();
}
