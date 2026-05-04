package com.example.dhcpms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 费用类型实体类
 */
@Data
@TableName("fee_type")
public class FeeType {
    @TableId(type = IdType.AUTO)
    private Long id;                // 主键
    private String typeName;        // 类型名称（如物业费/水电费/停车费等）
    private String remark;          // 备注
    private BigDecimal unitPrice;   // 单价（元/单位）
    private String calcType;        // 计费类型：1-关联计算、2-固定金额、3-自定义
    private String relatedField;    // 关联字段：多个字段用逗号分隔（HOUSE_AREA,INTERNAL_AREA 等）
    private String conditionType;   // 条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于
    private String conditionField;  // 条件字段：PARKING_SPACE_TYPE/HOUSE_USAGE 等
    private String conditionValue;  // 条件值
}
