package com.example.dhcpms.common;

/**
 * 报修状态枚举
 * 对应repair_order表的status字段
 */
public enum RepairStatusEnum {
    PENDING_REVIEW(0, "待审核"),
    ASSIGNED(1, "已审核"),
    UNDER_REPAIR(2, "维修中"),
    COMPLETED(3, "已完成"),
    CANCELED(4, "已取消（驳回）"),
    PENDING_ACCEPTANCE(5, "待验收"),
    ACCEPTED(6, "已验收");

    private final Integer code;
    private final String desc;

    RepairStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    // 根据编码获取描述
    public static String getDescByCode(Integer code) {
        if (code == null) {
            return "未知状态";
        }
        for (RepairStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status.getDesc();
            }
        }
        return "未知状态";
    }
}
