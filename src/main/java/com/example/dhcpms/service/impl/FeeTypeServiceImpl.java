package com.example.dhcpms.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dhcpms.entity.FeeType;
import com.example.dhcpms.mapper.FeeTypeMapper;
import com.example.dhcpms.service.FeeTypeService;
import org.springframework.stereotype.Service;

/**
 * 费用类型服务实现类
 */
@Service
public class FeeTypeServiceImpl extends ServiceImpl<FeeTypeMapper, FeeType> implements FeeTypeService {
}
