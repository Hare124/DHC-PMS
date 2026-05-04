/*
 Navicat Premium Data Transfer

 Source Server         : new
 Source Server Type    : MySQL
 Source Server Version : 80039 (8.0.39)
 Source Host           : localhost:3306
 Source Schema         : dhc_pms

 Target Server Type    : MySQL
 Target Server Version : 80039 (8.0.39)
 File Encoding         : 65001

 Date: 29/04/2026 04:04:19
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `title` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '公告标题',
  `content` text CHARACTER SET gb2312 COLLATE gb2312_bin NULL COMMENT '公告内容',
  `publisher_id` bigint NOT NULL COMMENT '发布人ID（关联 user 表）',
  `publish_time` datetime NOT NULL COMMENT '发布时间',
  `is_top` int NOT NULL DEFAULT 0 COMMENT '是否置顶（0-否/1-是）',
  `read_count` int NOT NULL DEFAULT 0 COMMENT '阅读次数',
  `recall_status` int NOT NULL DEFAULT 0 COMMENT '撤回状态：0-未撤回/1-已撤回',
  `recall_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_announcement_user`(`publisher_id` ASC) USING BTREE,
  CONSTRAINT `fk_announcement_user` FOREIGN KEY (`publisher_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (16, '关于小区绿化维护的通知', '尊敬的各位业主：为提升小区居住环境，物业将于 2026 年 3 月 25 日起对小区公共绿化区域进行春季养护，包括草坪修剪、树木整形、花卉补种等工作。养护期间可能会产生轻微噪音和短暂异味，敬请谅解。养护工作预计持续 7 天，每天工作时间为上午 8:30-12:00，下午 14:00-18:00。如有任何疑问或建议，欢迎联系物业前台。感谢您的理解与支持！', 11, '2026-04-03 17:37:02', 0, 79, 0, NULL, '2026-04-28 10:23:56', '2026-04-28 10:25:00');
INSERT INTO `announcement` VALUES (17, '小区门禁系统升级公告', '各位业主请注意：为提高小区安全防范水平，物业决定对小区门禁系统进行全面升级。升级时间：2026 年 3 月 28 日 22:00 至次日凌晨 6:00。升级期间，小区各出入口将暂时改为人工登记通行，请各位业主进出时配合安保人员的工作，携带好门禁卡或身份证件。系统升级后，将支持人脸识别、手机 NFC 等多种开门方式，具体使用说明将另行通知。由此给您带来的不便，深表歉意！', 11, '2026-03-03 17:37:23', 0, 370, 0, NULL, '2026-04-28 10:23:56', '2026-04-28 10:25:00');
INSERT INTO `announcement` VALUES (18, '2026 年清明节放假安排及小区服务通知', '尊敬的业主：根据国家法定节假日安排，结合小区实际情况，现将 2025 年清明节期间物业服务安排通知如下：一、放假时间：4 月 4 日（星期五）至 4 月 6 日（星期日）放假调休，共 3 天。二、服务安排：1. 物业前台 4 月 4 日暂停接待，4 月 5-6 日正常值班（值班时间：9:00-17:00）；2. 报修热线 400-123-4567 假期正常服务；3. 小区保洁、绿化养护工作照常进行；4. 安保工作 24 小时值守。三、温馨提示：清明祭祖请注意用火安全，外出时请关闭家中水电燃气阀门。祝您节日安康！', 11, '2026-04-03 17:37:53', 0, 155, 0, NULL, '2026-04-29 00:00:00', '2026-04-28 10:25:00');

-- ----------------------------
-- Table structure for announcement_read_record
-- ----------------------------
DROP TABLE IF EXISTS `announcement_read_record`;
CREATE TABLE `announcement_read_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `owner_user_id` bigint NOT NULL COMMENT '业主用户ID（关联user表）',
  `announcement_id` bigint NOT NULL COMMENT '公告ID（关联announcement表）',
  `read_time` datetime NOT NULL COMMENT '阅读时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_owner_announcement`(`owner_user_id` ASC, `announcement_id` ASC) USING BTREE,
  INDEX `fk_read_record_announcement`(`announcement_id` ASC) USING BTREE,
  INDEX `idx_read_record_owner_user_id`(`owner_user_id` ASC) USING BTREE,
  CONSTRAINT `fk_read_record_announcement` FOREIGN KEY (`announcement_id`) REFERENCES `announcement` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_read_record_user` FOREIGN KEY (`owner_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '业主公告已读记录扩展表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of announcement_read_record
-- ----------------------------

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '轮播图标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片 URL',
  `target_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
  `status` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1' COMMENT '状态（1-启用，0-禁用）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人 ID',
  `create_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人 ID',
  `update_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of carousel
-- ----------------------------
INSERT INTO `carousel` VALUES (1, '轮播图1', '/uploads/images/carousel/08.jpg', 'https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=1920&auto=format&fit=crop', 1, '1', 11, '王强', '2026-03-19 23:43:23', 11, '王强', '2026-04-28 18:43:04');
INSERT INTO `carousel` VALUES (5, '轮播图5', '/uploads/images/carousel/12.jpg', 'https://images.unsplash.com/photo-1600566753190-17f0baa2a6c3?w=1920&auto=format&fit=crop', 5, '0', 11, '王强', '2026-03-19 23:45:16', 11, '王强', '2026-04-22 23:46:08');
INSERT INTO `carousel` VALUES (6, '轮播图6', '/uploads/images/carousel/13.jpg', 'https://images.unsplash.com/photo-1600047509807-ba8f99d2cdde?w=1920&auto=format&fit=crop', 6, '0', 11, '王强', '2026-03-19 23:46:14', 11, '王强', '2026-04-22 23:46:00');
INSERT INTO `carousel` VALUES (7, '轮播图7', '/uploads/images/carousel/12.jpg', 'https://images.unsplash.com/photo-1600573472550-8090b5e0745e?w=1920&auto=format&fit=crop', 7, '0', 11, '王强', '2026-04-22 14:37:40', 11, '王强', '2026-04-22 23:47:35');
INSERT INTO `carousel` VALUES (8, '轮播图2', '/uploads/images/carousel/09.jpg', 'https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=1920&auto=format&fit=crop', 3, '1', 11, '王强', '2026-04-22 23:51:25', 11, '王强', '2026-04-22 23:54:04');
INSERT INTO `carousel` VALUES (9, '轮播图3', '/uploads/images/carousel/10.jpg', 'https://images.unsplash.com/photo-1600596542815-ffad4c1539a9?w=1920&auto=format&fit=crop', 1, '0', 11, '王强', '2026-04-22 23:52:18', 11, '王强', '2026-04-22 23:55:19');
INSERT INTO `carousel` VALUES (10, '轮播图4', '/uploads/images/carousel/11.jpg', 'https://images.unsplash.com/photo-1600607687939-ce8a6c25118c?w=1920&auto=format&fit=crop', 2, '1', 11, '王强', '2026-04-22 23:52:46', 11, '王强', '2026-04-22 23:53:29');

-- ----------------------------
-- Table structure for equipment
-- ----------------------------
DROP TABLE IF EXISTS `equipment`;
CREATE TABLE `equipment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `equipment_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编号',
  `equipment_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `equipment_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型（电梯、消防、供水、供电、照明、安防等）',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '型号',
  `install_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装位置',
  `building_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属楼栋号',
  `area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属区域',
  `purchase_date` date NULL DEFAULT NULL COMMENT '采购日期',
  `warranty_period` int NULL DEFAULT NULL COMMENT '质保期（月）',
  `service_life` int NULL DEFAULT NULL COMMENT '使用年限（年）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '设备状态（normal-正常、fault-故障、maintaining-维修中、disabled-停用、scrapped-报废）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_equipment_type`(`equipment_type` ASC) USING BTREE,
  INDEX `idx_equipment_status`(`status` ASC) USING BTREE,
  INDEX `idx_building_no`(`building_no` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '配套设备信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of equipment
-- ----------------------------
INSERT INTO `equipment` VALUES (1, 'DT-001', '1 号电梯', '电梯', '三菱', 'LEHY-II', '1 号楼 1 单元', '1 栋', 'A 区', '2023-01-15', 24, 15, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (2, 'DT-002', '2 号电梯', '电梯', '日立', 'HGP', '2 号楼 1 单元', '2 栋', 'A 区', '2023-02-20', 24, 15, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (3, 'XF-001', '消防泵 A', '消防', '凯泉', 'XBD8.0/20G', '地下泵房', NULL, 'B 区', '2022-11-10', 12, 10, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:21:23');
INSERT INTO `equipment` VALUES (4, 'XF-002', '消防栓箱', '消防', '天广', 'SN65', '各楼层通道', NULL, '全区', '2022-12-05', 12, 10, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (5, 'GS-001', '变频供水设备', '供水', '格兰富', 'MGE90', '水泵房', NULL, 'B 区', '2023-03-01', 24, 12, 'maintaining', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (6, 'GD-001', '1 号变压器', '供电', '特变电工', 'SCB10-800', '配电室', NULL, 'C 区', '2022-08-15', 36, 20, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (7, 'ZM-001', 'LED 路灯', '照明', '雷士', 'NVC-LED-100W', '小区主干道', NULL, '全区', '2023-04-10', 12, 8, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (8, 'ZM-002', '楼道灯', '照明', '欧普', 'OPPLE-18W', '各单元楼道', NULL, '全区', '2023-05-20', 12, 5, 'fault', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (9, 'AF-001', '监控摄像头', '安防', '海康威视', 'DS-2CD3T46', '小区出入口', NULL, 'A 区', '2023-06-01', 24, 8, 'normal', '2026-03-18 20:19:28', '2026-03-18 20:19:28');
INSERT INTO `equipment` VALUES (10, 'QT-001', '健身器材', '其他', '舒华', 'SH-T6600', '小区健身广场', NULL, 'D 区', '2022-10-01', 12, 10, 'disabled', '2026-03-18 20:19:28', '2026-03-18 20:19:28');

-- ----------------------------
-- Table structure for fee_record
-- ----------------------------
DROP TABLE IF EXISTS `fee_record`;
CREATE TABLE `fee_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `owner_id` bigint NOT NULL COMMENT '业主ID（关联 owner 表）',
  `fee_type_id` bigint NOT NULL COMMENT '费用类型ID（关联 fee_type 表）',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '减免金额',
  `late_fee` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '滞纳金',
  `actual_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '实收金额',
  `arrears_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '欠费金额',
  `due_date` date NOT NULL COMMENT '缴费截止日期',
  `status` int NULL DEFAULT 0 COMMENT '状态（0-未缴/1-已缴/2-撤销）',
  `pay_type` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '缴费方式（微信/支付宝/现金/银行卡/转账）',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '缴费时间',
  `transaction_no` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '缴费流水号',
  `operator_id` bigint NOT NULL COMMENT '物业操作员ID（关联 user 表）',
  `remark` varchar(500) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_fee_record_owner`(`owner_id` ASC) USING BTREE,
  INDEX `fk_fee_record_fee_type`(`fee_type_id` ASC) USING BTREE,
  INDEX `fk_fee_record_user`(`operator_id` ASC) USING BTREE,
  CONSTRAINT `fk_fee_record_fee_type` FOREIGN KEY (`fee_type_id`) REFERENCES `fee_type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_record_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_fee_record_user` FOREIGN KEY (`operator_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 71 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '费用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fee_record
-- ----------------------------
INSERT INTO `fee_record` VALUES (33, 13, 1, 100.00, 0.00, 0.00, 0.00, 0.00, '2026-03-10', 1, NULL, '2026-04-28 23:38:18', NULL, 11, NULL, '2026-03-11 18:12:34', '2026-04-28 23:38:17');
INSERT INTO `fee_record` VALUES (34, 13, 1, 188.25, 0.00, 0.00, 0.00, 0.00, '2026-03-10', 1, NULL, '2026-04-28 23:38:18', NULL, 11, NULL, '2026-03-11 19:18:58', '2026-04-28 23:38:17');
INSERT INTO `fee_record` VALUES (60, 12, 7, 15.00, 0.00, 0.00, 0.00, 0.00, '2026-04-05', 0, NULL, NULL, NULL, 11, NULL, '2026-04-04 00:44:04', '2026-04-04 00:44:04');
INSERT INTO `fee_record` VALUES (61, 13, 7, 15.00, 0.00, 0.00, 0.00, 0.00, '2026-04-05', 0, NULL, NULL, NULL, 11, NULL, '2026-04-04 00:44:04', '2026-04-04 00:44:04');
INSERT INTO `fee_record` VALUES (62, 11, 1, 127.50, 0.00, 0.00, 0.00, 0.00, '2026-05-31', 0, NULL, NULL, NULL, 11, NULL, '2026-04-24 00:13:32', '2026-04-24 00:13:32');
INSERT INTO `fee_record` VALUES (64, 13, 1, 188.25, 0.00, 0.00, 0.00, 0.00, '2026-05-31', 0, NULL, NULL, NULL, 11, NULL, '2026-04-24 00:13:32', '2026-04-24 00:13:32');
INSERT INTO `fee_record` VALUES (66, 11, 1, 127.50, 0.00, 0.00, 0.00, 0.00, '2026-05-31', 0, NULL, NULL, NULL, 11, NULL, '2026-04-24 16:22:19', '2026-04-24 16:22:19');
INSERT INTO `fee_record` VALUES (68, 13, 1, 188.25, 0.00, 0.00, 0.00, 0.00, '2026-05-31', 0, NULL, NULL, NULL, 11, NULL, '2026-04-24 16:22:19', '2026-04-24 16:22:19');
INSERT INTO `fee_record` VALUES (70, 12, 1, 180.75, 0.00, 0.00, 0.00, 0.00, '2026-04-28', 1, NULL, '2026-04-28 23:38:04', NULL, 11, NULL, '2026-04-28 23:37:03', '2026-04-28 23:38:04');

-- ----------------------------
-- Table structure for fee_reminder
-- ----------------------------
DROP TABLE IF EXISTS `fee_reminder`;
CREATE TABLE `fee_reminder`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `fee_record_id` bigint NOT NULL COMMENT '费用记录ID（关联 fee_record 表）',
  `reminder_time` datetime NOT NULL COMMENT '催缴时间',
  `reminder_type` int NOT NULL COMMENT '催缴类型（1-模板消息/2-短信）',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-未发送/1-已发送）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_fee_reminder_fee_record`(`fee_record_id` ASC) USING BTREE,
  CONSTRAINT `fk_fee_reminder_fee_record` FOREIGN KEY (`fee_record_id`) REFERENCES `fee_record` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '催缴记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fee_reminder
-- ----------------------------
INSERT INTO `fee_reminder` VALUES (36, 61, '2026-04-24 00:50:05', 1, 1);
INSERT INTO `fee_reminder` VALUES (37, 61, '2026-04-24 00:59:10', 1, 1);
INSERT INTO `fee_reminder` VALUES (40, 70, '2026-04-28 23:37:16', 2, 1);
INSERT INTO `fee_reminder` VALUES (41, 66, '2026-04-28 23:38:27', 1, 1);
INSERT INTO `fee_reminder` VALUES (42, 62, '2026-04-28 23:38:27', 1, 1);

-- ----------------------------
-- Table structure for fee_type
-- ----------------------------
DROP TABLE IF EXISTS `fee_type`;
CREATE TABLE `fee_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `type_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '费用类型名称（物业费/水电费/停车费等）',
  `unit_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '单价（元/平方米）',
  `calc_type` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT 'AREA' COMMENT '计费类型：AREA-按面积、FIXED-固定金额、CUSTOM-自定义',
  `related_field` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT 'HOUSE_AREA' COMMENT '关联字段：HOUSE_AREA-房屋建筑面积、INTERNAL_AREA-套内面积',
  `condition_type` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT 'NONE' COMMENT '条件类型：NONE-不设置、EQUAL-等于、NOT_EQUAL-不等于',
  `condition_field` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '条件字段：PARKING_SPACE_TYPE/HOUSE_USAGE 等',
  `condition_value` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '条件值',
  `remark` varchar(200) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '费用类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fee_type
-- ----------------------------
INSERT INTO `fee_type` VALUES (1, '物业费', 1.50, '1', 'HOUSE_AREA', 'NONE', NULL, NULL, '按建筑面积收取，包含公共区域保洁、安保、设施维护等费用，每月5日结算');
INSERT INTO `fee_type` VALUES (6, '暖气费', 26.00, '1', 'HOUSE_AREA', 'NONE', NULL, NULL, '冬季集中供暖费用，按建筑面积收取，单价26元/平方米，每年11月预缴');
INSERT INTO `fee_type` VALUES (7, '垃圾清运费', 15.00, '2', '', 'NONE', NULL, NULL, '生活垃圾处理费，每户每月15元，随物业费一并收取');
INSERT INTO `fee_type` VALUES (8, '电梯维护费', 20.00, '2', '', 'NONE', NULL, NULL, '电梯日常检修、保养、配件更换费用，按楼层阶梯收取，每月20元');
INSERT INTO `fee_type` VALUES (9, '公摊水电费', 20.00, '2', '', 'NONE', NULL, NULL, '小区公共区域（楼道、路灯、景观）水电分摊，按户数均摊，每月20元');
INSERT INTO `fee_type` VALUES (25, '车位管理费', 60.00, '1', 'PARKING_SPACE_TYPE', 'EQUAL', 'PARKING_SPACE_TYPE', '产权', '产权车位管理费，60 元/月');
INSERT INTO `fee_type` VALUES (27, '停车租赁费', 300.00, '1', 'PARKING_SPACE_TYPE', 'EQUAL', 'PARKING_SPACE_TYPE', '租赁', '租赁车位300元/月');
INSERT INTO `fee_type` VALUES (28, '装修保证金', 2000.00, '2', '', 'NONE', '', NULL, '押金，装修完可退');

-- ----------------------------
-- Table structure for owner
-- ----------------------------
DROP TABLE IF EXISTS `owner`;
CREATE TABLE `owner`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `user_id` bigint NOT NULL COMMENT '用户ID（关联 user 表）',
  `building_no` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '楼栋号',
  `room_no` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '房间号',
  `house_area` decimal(10, 2) NULL DEFAULT NULL COMMENT '房屋建筑面积（平方米）',
  `internal_area` decimal(10, 2) NULL DEFAULT NULL COMMENT '套内建筑面积（平方米）',
  `house_layout` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '房屋户型',
  `house_usage` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '房屋用途（住宅/商业/办公）',
  `house_structure` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '房屋结构（砖混/框架等）',
  `ownership_type` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '共有情况（单独所有/共同共有/按份共有）',
  `co_owner_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '共有人姓名',
  `co_owner_id_card` varchar(18) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '共有人身份证号',
  `property_cert_no` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '不动产权证书号',
  `property_unit_no` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '不动产单元号',
  `registration_date` date NULL DEFAULT NULL COMMENT '登记日期',
  `registration_authority` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '发证机关',
  `land_use_years` int NULL DEFAULT NULL COMMENT '土地使用年限',
  `land_nature` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '土地性质',
  `is_mortgaged` tinyint(1) NULL DEFAULT 0 COMMENT '是否抵押',
  `mortgage_amount` decimal(15, 2) NULL DEFAULT NULL COMMENT '抵押金额',
  `mortgagee_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '抵押权人姓名',
  `is_seized` tinyint(1) NULL DEFAULT 0 COMMENT '是否查封',
  `has_residence_right` tinyint(1) NULL DEFAULT 0 COMMENT '是否设立居住权',
  `delivery_date` date NULL DEFAULT NULL COMMENT '房屋交付日期',
  `registered_usage` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '产权登记用途',
  `has_parking_space` tinyint(1) NULL DEFAULT 0 COMMENT '是否配套车位',
  `has_storage_room` tinyint(1) NULL DEFAULT 0 COMMENT '是否配套储藏室',
  `parking_space_type` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '车位产权类型',
  `parking_space_no` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '车位编号',
  `id_card` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '身份证号',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_owner_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_owner_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '业主信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of owner
-- ----------------------------
INSERT INTO `owner` VALUES (11, 26, '1栋', '0803', 85.00, 72.50, '一室一厅一卫', '住宅', '框架', '单独所有', NULL, NULL, '粤 (2024) 广州市不动产权第 0001237 号', '440101005001GB00005F00010021', '2019-05-18', '广州市规划和自然资源局', 50, '出让', 0, NULL, NULL, 0, 0, '2019-06-30', '住宅', 0, 0, NULL, NULL, '110101199001011234', '2026-02-14 15:10:03');
INSERT INTO `owner` VALUES (12, 12, '1栋', '2605', 120.50, 105.30, '三室两厅两卫', '住宅', '框架', '单独所有', '', '', '粤 (2023) 广州市不动产权第 0004594 号', '440101005001GB00005F00010033', '2024-01-14', '广州市规划和自然资源局', 70, '出让', 0, NULL, '', 0, 0, '2023-10-30', '住宅', 1, 0, '产权', 'B2-020', '110101198502022345', '2026-02-14 15:12:08');
INSERT INTO `owner` VALUES (13, 36, '1栋', '1001', 125.50, 98.60, '三室两厅两卫', '住宅', '框架', '单独所有', NULL, '440101199001011256', '粤 (2024) 广州市不动产权第 0001234 号', '440101001001GB00001F00010001', '2024-01-15', '广州市规划和自然资源局', 70, '出让', 0, NULL, NULL, 0, 0, '2024-03-01', '住宅', 1, 1, '产权', 'A2-001', '110101199001011235', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (14, 2, '1栋', '1002', 89.00, 72.30, '两室两厅一卫', '住宅', '框架', '共同共有', '张三', '440101199001011234', '粤 (2024) 广州市不动产权第 0001235 号', '440101001001GB00001F00010002', '2024-02-20', '广州市规划和自然资源局', 70, '出让', 1, 1500000.00, '中国工商银行广州分行', 0, 0, '2024-04-15', '住宅', 1, 0, '产权', 'A1-001', '110101198502022347', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (15, 27, '2栋', '2001', 156.80, 128.50, '四室两厅三卫', '住宅', '砖混', '按份共有', '李四', '440101198502022345', '粤 (2023) 广州市不动产权第 0005678 号', '440101002001GB00002F00010001', '2023-11-10', '广州市规划和自然资源局', 70, '出让', 1, 2800000.00, '中国建设银行广州分行', 0, 1, '2023-12-20', '住宅', 1, 1, '产权', 'A2-002', '110101199203033478', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (16, 4, '2栋', '2002', 65.00, 52.00, '一室一厅一卫', '商业', '框架', '单独所有', NULL, NULL, '粤 (2024) 广州市不动产权第 0009012 号', '440101002001GB00002F00010002', '2024-03-05', '广州市规划和自然资源局', 40, '出让', 0, NULL, NULL, 0, 0, '2024-05-01', '商业', 0, 0, NULL, NULL, '110101198804044589', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (17, 28, '3栋', '3001', 200.00, 165.00, '五室三厅三卫', '住宅', '框架', '共同共有', '王五', '440101198803033456', '粤 (2023) 广州市不动产权第 0003456 号', '440101003001GB00003F00010001', '2023-09-18', '广州市规划和自然资源局', 70, '出让', 1, 4500000.00, '中国农业银行广州分行', 0, 0, '2023-10-30', '住宅', 1, 1, '产权', 'A2-003', '110101199505055690', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (18, 6, '3栋', '3002', 78.50, 63.20, '两室两厅一卫', '住宅', '砖混', '单独所有', NULL, NULL, '粤 (2024) 广州市不动产权第 0007890 号', '440101003001GB00003F00010002', '2024-01-25', '广州市规划和自然资源局', 70, '出让', 0, NULL, NULL, 0, 1, '2024-02-28', '住宅', 0, 1, NULL, NULL, '110101199505055123', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (19, 7, '4栋', '3401', 110.00, 88.00, '三室两厅一卫', '办公', '框架', '按份共有', '赵六', '440101199204044567', '粤 (2023) 广州市不动产权第 0002345 号', '440101004001GB00004F00010001', '2023-12-08', '广州市规划和自然资源局', 50, '出让', 1, 2000000.00, '招商银行广州分行', 0, 0, '2024-01-10', '办公', 1, 0, '租赁', 'A1-002', '110101199505055124', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (20, 29, '4栋', '1502', 145.60, 118.30, '四室两厅两卫', '住宅', '框架', '共同共有', '钱七', '440101198605055678', '粤 (2024) 广州市不动产权第 0006789 号', '440101004001GB00004F00010002', '2024-02-14', '广州市规划和自然资源局', 70, '出让', 0, NULL, NULL, 1, 0, '2024-03-20', '住宅', 1, 1, '产权', 'A1-003', '110101199505055156', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (21, 9, '5栋', '0901', 95.00, 76.50, '三室两厅一卫', '住宅', '砖混', '单独所有', NULL, NULL, '粤 (2024) 广州市不动产权第 0008901 号', '440101005001GB00005F00010001', '2024-03-12', '广州市规划和自然资源局', 70, '划拨', 0, NULL, NULL, 0, 1, '2024-04-25', '住宅', 0, 0, NULL, NULL, '110101199505055178', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (22, 10, '5栋', '0302', 180.00, 148.00, '四室三厅两卫', '住宅', '框架', '按份共有', '孙八', '440101199006066789', '粤 (2023) 广州市不动产权第 0004567 号', '440101005001GB00005F00010002', '2023-10-22', '广州市规划和自然资源局', 70, '出让', 1, 3200000.00, '中国银行广州分行', 0, 0, '2023-11-28', '住宅', 1, 1, '产权', 'A1-004', '110101199505055189', '2026-03-08 20:52:14');
INSERT INTO `owner` VALUES (23, 13, '1栋', '1101', 120.50, 98.30, '三室两厅两卫', '住宅', '框架', '单独所有', '', '', '京 (2024) 朝不动产权第 0001234 号', '110105001001GB00001F00010001', '2024-01-14', '北京市朝阳区不动产登记中心', 70, '出让', 0, NULL, '', 0, 0, '2023-12-30', '住宅', 1, 1, '产权', 'B2-001', '110101199001011234', '2026-03-09 16:18:13');
INSERT INTO `owner` VALUES (24, 14, '2栋', '2003', 85.00, 72.50, '一室一厅一卫', '商业', '钢结构', '共同共有', '王五', '310101198606063456', '沪 (2023) 浦不动产权第 0005678 号', '310115002002GB00002F00020003', '2023-06-19', '上海市浦东新区不动产登记中心', 40, '出让', 1, 2000000.00, '中国银行上海分行', 0, 0, '2023-04-30', '商业', 1, 0, '租赁', 'B1-056', '310101198505052345', '2026-03-09 16:25:33');
INSERT INTO `owner` VALUES (25, 15, '3栋', '1505', 200.00, 180.00, '五室两厅三卫', '办公', '框架', '按份共有', '赵六', '440101198909095678', '粤 (2022) 广不动产权第 0009012 号', '440106003003GB00003F00150005', '2022-11-09', '广州市天河区不动产登记中心', 50, '出让', 1, 5000000.00, '中国工商银行广州分行', 0, 1, '2022-09-30', '办公', 1, 1, '产权', 'B3-102', '440101198808084567', '2026-03-09 16:28:12');
INSERT INTO `owner` VALUES (26, 16, '5栋', '0802', 150.00, 125.00, '四室两厅两卫', '住宅', '砖混', '单独所有', '', '', '川 (2021) 成不动产权第 0003456 号', '510107004004GB00004F00080002', '2021-08-24', '成都市武侯区不动产登记中心', 70, '划拨', 0, NULL, '', 1, 0, '2021-06-30', '住宅', 0, 0, '', '', '510101199212126789', '2026-03-09 16:31:36');
INSERT INTO `owner` VALUES (27, 17, '6栋', '2601', 280.00, 250.00, '六室三厅四卫', '住宅', '框架', '共同共有', '周八', '330101199604048901', '浙 (2020) 杭不动产权第 0007890 号', '330108005005GB00005F00260001', '2020-05-17', '杭州市西湖区不动产登记中心', 70, '出让', 1, 8000000.00, '中国建设银行杭州分行', 0, 1, '2020-03-31', '住宅', 1, 1, '产权', 'B2-201', '330101199503037890', '2026-03-09 16:34:03');
INSERT INTO `owner` VALUES (35, 34, '1栋', '1005', 111.00, 104.00, '三室两厅三卫', '住宅', '砖混', '单独所有', '', '', '', '', NULL, '', NULL, '', 0, NULL, '', 0, 0, NULL, '', 0, 0, '', '', '110101199001011236', '2026-04-28 23:35:58');

-- ----------------------------
-- Table structure for owner_message
-- ----------------------------
DROP TABLE IF EXISTS `owner_message`;
CREATE TABLE `owner_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `chat_room_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '群聊名称',
  `sender_id` bigint NOT NULL COMMENT '发送人 ID（用户 ID）',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送人姓名',
  `message_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text' COMMENT '消息类型：text-文本/image-图片/file-文件',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件 URL',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件名称',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `is_withdrawn` tinyint(1) NULL DEFAULT 0 COMMENT '是否已撤回：0-否/1-是',
  `withdraw_time` datetime NULL DEFAULT NULL COMMENT '撤回时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_room`(`chat_room_name` ASC) USING BTREE,
  INDEX `idx_send_time`(`send_time` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  CONSTRAINT `fk_owner_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业主群聊消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of owner_message
-- ----------------------------
INSERT INTO `owner_message` VALUES (15, '鼎湖社区业主群', 13, 'owner07', '各位邻居好，请问谁知道附近哪里有好的幼儿园推荐吗？', 'text', NULL, NULL, '2026-03-18 13:00:00', 0, NULL, '2026-03-18 13:00:00');
INSERT INTO `owner_message` VALUES (16, '鼎湖社区业主群', 14, 'owner08', '我觉得阳光幼儿园不错，距离近，教学质量也可以', 'text', NULL, NULL, '2026-03-18 13:05:00', 0, NULL, '2026-03-18 13:05:00');
INSERT INTO `owner_message` VALUES (17, '鼎湖社区业主群', 15, 'owner09', '大家好，我是 1 号楼新搬来的业主，请多关照', 'text', NULL, NULL, '2026-03-18 14:00:00', 0, NULL, '2026-03-18 14:00:00');
INSERT INTO `owner_message` VALUES (18, '鼎湖社区业主群', 16, 'owner10', '欢迎欢迎！以后有什么事都可以在群里说', 'text', NULL, NULL, '2026-03-18 14:05:00', 0, NULL, '2026-03-18 14:05:00');
INSERT INTO `owner_message` VALUES (19, '鼎湖社区业主群', 17, '孙七', '通知：本周五下午 2 点在小区会议室召开业主大会，请大家准时参加', 'text', NULL, NULL, '2026-03-18 15:00:00', 0, NULL, '2026-03-18 15:00:00');
INSERT INTO `owner_message` VALUES (20, '鼎湖社区业主群', 13, 'owner11', '收到，会准时参加的', 'text', NULL, NULL, '2026-03-18 15:10:00', 0, NULL, '2026-03-18 15:10:00');
INSERT INTO `owner_message` VALUES (21, '鼎湖社区业主群', 13, 'owner12', '请问有谁知道物业电话是多少吗？', 'text', NULL, NULL, '2026-03-18 16:00:00', 0, NULL, '2026-03-18 16:00:00');
INSERT INTO `owner_message` VALUES (22, '鼎湖社区业主群', 20, 'owner13', '物业电话是 010-12345678', 'text', NULL, NULL, '2026-03-18 16:05:00', 0, NULL, '2026-03-18 16:05:00');
INSERT INTO `owner_message` VALUES (23, '鼎湖社区业主群', 21, 'owner14', '最近小区里的流浪猫比较多，大家有什么好的建议吗？', 'text', NULL, NULL, '2026-03-18 17:00:00', 0, NULL, '2026-03-18 17:00:00');
INSERT INTO `owner_message` VALUES (24, '鼎湖社区业主群', 22, 'owner15', '可以联系物业看看能不能统一安置一下', 'text', NULL, NULL, '2026-03-18 17:10:00', 0, NULL, '2026-03-18 17:10:00');
INSERT INTO `owner_message` VALUES (25, '鼎湖社区业主群', 12, '蒋十五', '分享一张小区的美景', 'file', '/uploads/images/carousel/01.png', '小区美景.jpg', '2026-03-18 18:00:00', 0, NULL, '2026-03-18 18:00:00');
INSERT INTO `owner_message` VALUES (26, '鼎湖社区业主群', 13, 'owner07', '分享一份业主公约文档', 'file', '/uploads/files/community/owner_agreement.pdf', '业主公约.pdf', '2026-03-18 18:30:00', 0, NULL, '2026-03-18 18:30:00');
INSERT INTO `owner_message` VALUES (27, '鼎湖社区业主群', 14, 'owner08', '提醒：明天上午 9 点到 11 点停水，大家记得提前储水', 'text', NULL, NULL, '2026-03-18 19:00:00', 0, NULL, '2026-03-18 19:00:00');
INSERT INTO `owner_message` VALUES (28, '鼎湖社区业主群', 15, 'owner09', '好的，谢谢提醒！', 'text', NULL, NULL, '2026-03-18 19:05:00', 0, NULL, '2026-03-18 19:05:00');
INSERT INTO `owner_message` VALUES (29, '鼎湖社区业主群', 16, 'owner10', '谁家有闲置的儿童自行车可以借用一下吗？', 'text', NULL, NULL, '2026-03-18 20:00:00', 0, NULL, '2026-03-18 20:00:00');
INSERT INTO `owner_message` VALUES (30, '鼎湖社区业主群', 17, '孙七', '我家有一辆，明天给你送过来', 'text', NULL, NULL, '2026-03-18 20:10:00', 0, NULL, '2026-03-18 20:10:00');
INSERT INTO `owner_message` VALUES (31, '鼎湖社区业主群', 13, 'owner11', '建议大家不要在高楼层抛物，很危险的', 'text', NULL, NULL, '2026-03-18 21:00:00', 0, NULL, '2026-03-18 21:00:00');
INSERT INTO `owner_message` VALUES (32, '鼎湖社区业主群', 13, 'owner12', '赞同，安全问题不能忽视', 'text', NULL, NULL, '2026-03-18 21:10:00', 0, NULL, '2026-03-18 21:10:00');
INSERT INTO `owner_message` VALUES (33, '鼎湖社区业主群', 20, 'owner13', '周末有邻居一起去爬山吗？', 'text', NULL, NULL, '2026-03-18 22:00:00', 0, NULL, '2026-03-18 22:00:00');
INSERT INTO `owner_message` VALUES (34, '鼎湖社区业主群', 21, 'owner14', '报名 +1，好久没运动了', 'text', NULL, NULL, '2026-03-18 22:10:00', 0, NULL, '2026-03-18 22:10:00');
INSERT INTO `owner_message` VALUES (38, '鼎湖社区业主群', 12, '蒋十五', '1111', 'text', NULL, NULL, '2026-04-28 18:09:48', 0, NULL, '2026-04-28 18:09:48');
INSERT INTO `owner_message` VALUES (39, '鼎湖社区业主群', 12, '蒋十五', '2222', 'text', NULL, NULL, '2026-04-28 23:30:10', 0, NULL, '2026-04-28 23:30:10');

-- ----------------------------
-- Table structure for regulation
-- ----------------------------
DROP TABLE IF EXISTS `regulation`;
CREATE TABLE `regulation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `regulation_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '制度编号',
  `regulation_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '制度名称',
  `regulation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '制度类型（收费管理/消防安全/装修管理/车辆管理/其他）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '制度内容（富文本）',
  `attachment_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件 URL',
  `attachment_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '附件名称',
  `apply_scope` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用范围（全体业主/某栋楼）',
  `effective_date` date NULL DEFAULT NULL COMMENT '生效日期',
  `invalid_date` date NULL DEFAULT NULL COMMENT '失效日期',
  `version` int NULL DEFAULT 1 COMMENT '版本号',
  `parent_regulation_id` bigint NULL DEFAULT NULL COMMENT '父制度 ID（版本控制）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'draft' COMMENT '状态（draft-草稿/published-已发布/suspended-已下架/invalid-已作废）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人 ID',
  `create_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人姓名',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人 ID',
  `update_by_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人姓名',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_regulation_type`(`regulation_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_effective_date`(`effective_date` ASC) USING BTREE,
  INDEX `idx_parent_regulation`(`parent_regulation_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物业制度信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of regulation
-- ----------------------------
INSERT INTO `regulation` VALUES (2, 'WY-YG-0002', '员工行为规范及小区设施管理制度', '员工纪律', '一、目的：\n　　规范全体员工行为，加强本公司员工队伍的建设，提高员工的基本素质。　　\n二、适用范围：\n　　山东万众物业有限公司所有工作人员。　　\n三、职责：\n　　山东万众物业有限公司的所有员工有义务严守规章制度、为公司利益而做出贡献。\n　　各部门负责人要对员工进行规章制度教育，并全面贯彻下去。　　\n四、关于服装：\n　　装束进公司必须穿好工作服；工作服要干净；进公司须戴工作证；严禁工作证借给别人或借别人工作证入公司；凡遗失工作证、工作服应尽快申请补领；不得擅自涂改工作证，若要更改一定要经人事部办理；公司发放的安全鞋仅限在厂内使用；公司发放的作业服、安全鞋、围裙等妥善保管使用。　　\n五、关于上班时间：\n　　时间为上午8：30----12：00\n　　下午2：00----18：00不得无故迟到、早退、外出；雇员请假须提前一天通知部门主管批准后方可请假（特殊情况例外）；严禁无故旷工；上、下班必须签到；不许代别人签到；不得涂改签到表。　　\n六、关于小区设备和设施：\n　　设备、设施要小心使用，应保持设备整洁美观；注意整理整顿；道路要保持畅通，不许摆放东西；严禁对小区内的设备乱涂、张贴；节约用水，用电，不得浪费；凡故意破坏设备、设施，从严处理；凡盗窃本公司财物，从严处理；设备引起故障时必须要及时报告相关领导，不得擅动机器。　　\n七、其它服从上司的命令：\n　　发生事情必须立即上报、联络；严禁在花园内吐痰或丢垃圾；严禁在花园内吸烟；不准喧哗、赌博；不准盗窃他人或公司内的东西；\n', '', '', '物业人员', '2026-03-08', '2027-01-06', 1, NULL, 'invalid', 21, '梁经理', '2026-03-19 22:19:13', 21, '梁经理', '2026-03-19 22:38:45');
INSERT INTO `regulation` VALUES (3, 'WY-YG-0002-V2', '员工行为规范及小区设施管理制度', '员工纪律', '一、目的：\n　　规范全体员工行为，加强本公司员工队伍的建设，提高员工的基本素质。　　\n二、适用范围：\n　　山东万众物业有限公司所有工作人员。　　\n三、职责：\n　　山东万众物业有限公司的所有员工有义务严守规章制度、为公司利益而做出贡献。\n　　各部门负责人要对员工进行规章制度教育，并全面贯彻下去。　　\n四、关于服装：\n　　装束进公司必须穿好工作服；工作服要干净；进公司须戴工作证；严禁工作证借给别人或借别人工作证入公司；凡遗失工作证、工作服应尽快申请补领；不得擅自涂改工作证，若要更改一定要经人事部办理；公司发放的安全鞋仅限在厂内使用；公司发放的作业服、安全鞋、围裙等妥善保管使用。　　\n五、关于上班时间：\n　　时间为上午8：30----12：00\n　　下午2：00----18：00不得无故迟到、早退、外出；雇员请假须提前一天通知部门主管批准后方可请假（特殊情况例外）；严禁无故旷工；上、下班必须签到；不许代别人签到；不得涂改签到表。　　\n六、关于小区设备和设施：\n　　设备、设施要小心使用，应保持设备整洁美观；注意整理整顿；道路要保持畅通，不许摆放东西；严禁对小区内的设备乱涂、张贴；节约用水，用电，不得浪费；凡故意破坏设备、设施，从严处理；凡盗窃本公司财物，从严处理；设备引起故障时必须要及时报告相关领导，不得擅动机器。　　\n七、其它服从上司的命令：\n　　发生事情必须立即上报、联络；严禁在花园内吐痰或丢垃圾；严禁在花园内吸烟；不准喧哗、赌博；不准盗窃他人或公司内的东西；\n', '', '', '物业人员', '2026-03-15', '2027-03-26', 2, 2, 'suspended', 21, '梁经理', '2026-03-19 22:36:06', NULL, NULL, '2026-03-19 22:38:53');
INSERT INTO `regulation` VALUES (4, 'WY-YG-0002-V2-V3', '员工行为规范及小区设施管理制度', '员工纪律', '一、目的：\n　　规范全体员工行为，加强本公司员工队伍的建设，提高员工的基本素质。　　\n二、适用范围：\n　　山东万众物业有限公司所有工作人员。　　\n三、职责：\n　　山东万众物业有限公司的所有员工有义务严守规章制度、为公司利益而做出贡献。\n　　各部门负责人要对员工进行规章制度教育，并全面贯彻下去。　　\n四、关于服装：\n　　装束进公司必须穿好工作服；工作服要干净；进公司须戴工作证；严禁工作证借给别人或借别人工作证入公司；凡遗失工作证、工作服应尽快申请补领；不得擅自涂改工作证，若要更改一定要经人事部办理；公司发放的安全鞋仅限在厂内使用；公司发放的作业服、安全鞋、围裙等妥善保管使用。　　\n五、关于上班时间：\n　　时间为上午8：30----12：00\n　　下午2：00----18：00不得无故迟到、早退、外出；雇员请假须提前一天通知部门主管批准后方可请假（特殊情况例外）；严禁无故旷工；上、下班必须签到；不许代别人签到；不得涂改签到表。　　\n六、关于小区设备和设施：\n　　设备、设施要小心使用，应保持设备整洁美观；注意整理整顿；道路要保持畅通，不许摆放东西；严禁对小区内的设备乱涂、张贴；节约用水，用电，不得浪费；凡故意破坏设备、设施，从严处理；凡盗窃本公司财物，从严处理；设备引起故障时必须要及时报告相关领导，不得擅动机器。　　\n七、其它服从上司的命令：\n　　发生事情必须立即上报、联络；严禁在花园内吐痰或丢垃圾；严禁在花园内吸烟；不准喧哗、赌博；不准盗窃他人或公司内的东西；\n', '', '', '物业人员', '2026-03-19', '2027-04-01', 3, 2, 'published', 21, '梁经理', '2026-03-19 22:38:16', NULL, NULL, '2026-03-19 22:38:55');
INSERT INTO `regulation` VALUES (5, 'WY-YG-0002-V2-V3-V4', '员工行为规范及小区设施管理制度', '员工纪律', '一、目的：\n　　规范全体员工行为，加强本公司员工队伍的建设，提高员工的基本素质。　　\n二、适用范围：\n　　山东万众物业有限公司所有工作人员。　　\n三、职责：\n　　山东万众物业有限公司的所有员工有义务严守规章制度、为公司利益而做出贡献。\n　　各部门负责人要对员工进行规章制度教育，并全面贯彻下去。　　\n四、关于服装：\n　　装束进公司必须穿好工作服；工作服要干净；进公司须戴工作证；严禁工作证借给别人或借别人工作证入公司；凡遗失工作证、工作服应尽快申请补领；不得擅自涂改工作证，若要更改一定要经人事部办理；公司发放的安全鞋仅限在厂内使用；公司发放的作业服、安全鞋、围裙等妥善保管使用。　　\n五、关于上班时间：\n　　时间为上午8：30----12：00\n　　下午2：00----18：00不得无故迟到、早退、外出；雇员请假须提前一天通知部门主管批准后方可请假（特殊情况例外）；严禁无故旷工；上、下班必须签到；不许代别人签到；不得涂改签到表。　　\n六、关于小区设备和设施：\n　　设备、设施要小心使用，应保持设备整洁美观；注意整理整顿；道路要保持畅通，不许摆放东西；严禁对小区内的设备乱涂、张贴；节约用水，用电，不得浪费；凡故意破坏设备、设施，从严处理；凡盗窃本公司财物，从严处理；设备引起故障时必须要及时报告相关领导，不得擅动机器。　　\n七、其它服从上司的命令：\n　　发生事情必须立即上报、联络；严禁在花园内吐痰或丢垃圾；严禁在花园内吸烟；不准喧哗、赌博；不准盗窃他人或公司内的东西；\n', '', '', '物业人员', '2026-03-19', '2027-03-27', 4, 2, 'draft', 21, '梁经理', '2026-03-19 22:39:03', NULL, NULL, '2026-03-19 22:39:03');
INSERT INTO `regulation` VALUES (6, 'WY-YG-0001', '员工工作纪律及保安员岗位职责制度', '岗位职责', '一、员工工作纪律\n　　1、物业工作人员必须按时上下班，不准迟到、早退、旷工。\n　　2、必须按规定着装、佩证上岗。\n　　3、各级员工都必须切实服从上级领导的工作布置，不得随意改变、无故拖延、拒绝或终止工作。\n　　4、各级员工都应遵循逐级请示报告的原则，非特殊情况不得越级请示报告工作。\n　　5、不擅自离岗，上班不得睡觉。\n　　6、员工调离或辞职、辞退、离开公司工作时，应将公司配备物品、钥匙等交公司办公室。\n　　7、工作期间不准擅自离开自己的工作岗位，更不允许干其它私活　　\n二、保安员岗位职责\n　　1、维护大门口的交通秩序，引导车辆行驶及行人过往，保障车辆和行人的安全、使门前畅通无阻；\n　　2、着装整齐、精神饱满、仪态大方，热情、礼貌、周到地回答来访人的询问，严禁用粗言恶语对待来访者；\n　　3、严格把好第一关，高度警戒，发现精神病患者、衣衫不整或行踪可疑者，坚决拦阻其进入蔬菜大厦；\n　　4、时刻保持高度警惕，切实做好门前的警戒工作。注意车辆和行人的安全，人多时要注意防止失窃，防止大门口周围有闹事、斗殴事件发生，确保门前安全。\n　　5、在值班过程中对发现的问题及时处理，紧急情况及时向公司领导汇报，必要时向公安部门报警。\n　　6、及时做好当班值班巡视记录，不得弄虚作假。\n　　7、制止闲杂人员、小商贩、推销员私自进入。\n　　8、对出入蔬菜大厦的人员做好检查登记，严格按照集团管理规定执行，不得偷懒，耍滑。\n　　9、建立健全的防火、防盗、应付突发事件的措施，发生应急事态，', '', '', '物业人员', '2026-03-19', '2028-03-24', 1, NULL, 'published', 21, '梁经理', '2026-03-19 22:42:01', 21, '梁经理', '2026-03-19 23:03:30');
INSERT INTO `regulation` VALUES (7, 'WY-YG-0003', '部门职责及核心岗位工作制度', '岗位职责', '第一章总则\n　　物业管理部是公司开发项目设备保障、设备正常运行、公司后勤保障的职能部门。物业部的管理具有严密的科学性和较高的技术性，是为顾客及公司产品创造安全、文明、舒适、方便的经营及消费环境的基本保证和坚强后盾，是反映公司服务水准、良好形象和声誉的重要标志部门。　　\n第二章部门职责主要岗位及标准\n　　一、主要岗位职责\n　　总经理职责\n　　1、决定公司的发展规划、年度经营计划和投资方案；\n　　2、决定公司的经营方针和管理机构设?；\n　　3、确认公司年度财务预、决算等方案；\n　　4、主持公司的经营管理工作，分管人事部、安保部、工程部、保洁部、财务部；\n　　5、拟订公司年度经营计划草案，并组织实施；\n　　6、拟订公司基本管理制度和内部管理机构设?方案；\n　　7、拟订公司规章制度方案；\n　　8、全面负责公司对外联系部门和行业管理部门的沟通及关系；\n　　9、全面考核物业员工的日常工作内容和状态。\n　　总经理助理职责\n　　1、拟草物业管理月度和年度计划，报总经理审批执行；\n　　2、协助总经理处理公司日常工作，协调物业各部门工作；\n　　3、协助总经理制定公司年度财务预、决算等方案；\n　　4、负责公司质量管理工作，工程、保洁、保安各部门工作落实情况；\n　　5、负责物业人事管理及工资核算工作；\n　　6、协助总经理制定基本管理制度和内部管理方案；\n　　7、管理租户合同到期的续租及善后事宜；\n　　8、协助总经理与外联管理部门、行业管理部门的有关工作；\n　　9、执行总经理交办的工作。\n　　客服文员职责\n　　1、各部门的周检和月检抽查工作，建立物业资料档案；\n　　2、负责核收租户的收费，及时发现收费中的问题并帮助解决；\n　　3、负责处理租户上报的重要投诉；\n　　4、负责协调处理各部门上报的与人事、维修、安防、保洁等有关事务并作记录；\n　　5、负责制定质量管理计划和创建计划并指导实施；\n　　6、文件收发，送阅、存档。文件资料的打印、复印；\n　　7、档案管理，公司财产管理和办公室的管理；\n　　8、会议组织，整理会议纪要；\n　　9、负责接待和有关内务工作；\n　　10、处理业主的投诉，并做好跟踪和回访工作；\n　　11、分管领导交办的工作。\n　　保安部岗位职责\n　　1、按规定着装，整齐干净，注意仪容仪表；\n　　2、严格交接班手续，认真完成交接班记录；\n　　3、做好车辆停放及管理交接手续；\n　　4、按时巡楼，填写防火巡查表；\n　　5、管理好大厦物品出入楼登记工作；\n　　6、按时到正装修租户施工现场进行巡逻管理；\n　　7、礼貌待人，文明执勤，不得与租户发生争吵；\n　　8、工作时间不得玩手机、脱岗、睡觉，一经发现，一律惩处；\n　　9、爱护工作设备，妥善保管使用，熟练操作消防设施设备；\n　　10、认真完成上下班签到制度，不得无故迟到早退；\n　　11、完成上级交办的其他临时工作任务。\n　　工程部岗位职责\n　　1、严格遵守公司各项规章制度，服从领导安排，除完成日常维修任务外，有计划地承担其它工作任务；\n　　2、努力学习技术，熟练掌握大厦强弱电设备，工作原理及实操作与维修保养；\n　　3、执行所管辖设备检修计划，按时按量完成，并填好记录表；\n　　4、积极配合管理部门工作，出现上下水故障时能够迅速排除故障；\n　　5、严格执行设备管理制度，做好交接班工作；\n　　6、交班时发生故障，上班必须协同下班工作，排除故障后才能离去；\n　　7、维修人员外出巡查或维修，必须随身携带对讲机，随时随地与部门人员保持联系；\n　　8、在巡查过程中，发现不正常现象，应及时进行处理，处理不了的应做好记录，不得隐瞒故障现象；\n　　9、做好办公室清洁工作；\n　　10、当天因为配件不全的原因无法修复的作好记录，隔天修复；\n　　11、配电房是大厦供电系统的关键部位，未经主管人员以上许可，非工作人员不准入内；\n　　12、值班是必须持证上岗，熟悉配电设备状况，操作方法和安全注意事项；\n　　13、值班员必须密切注意电压表、电流表、功率因数表的指示情况，严禁变压器、空气开关超载运行；\n　　14、配电房设备的倒闸操作由值班员单独进行，其他在场人员只作监护，不得插手，严禁两人同时进行，以免发生错误；\n　　15、因故停电，应提前一天向使用用户发生停电通知，对突发性停电事故，应通过电话、口头通知或广播向用户作出解释；\n　　16、保持配电房消防设备完好齐备，保证应急灯在停电状态下能正常启用；\n　　17、严禁在配电房内私拉乱接，随意改变供电运行方式；\n　　18、配电房内严禁堆放其它杂物；\n　　19、完成领导交办的其它工作。\n　　保洁部岗位职责\n　　1、在岗工作时工装整洁，按规定上下班；\n　　2、做好日常工作的同时，发现灯具、洁具有破损现象及时报告；\n　　3、爱护工具，妥善保管；\n　　4、消防物品、标示牌、灭火箱要经常清洁；\n　　5、收集垃圾及时处理。避免过多外溢，产生异味，影响环境卫生；\n　　6、卫生间每小时巡查一次，做到地面干净无异味；\n　　7、电梯间随时进行巡查，保持按键无污渍，地面无烟头、纸屑、水渍等杂物，电梯门沟无异物；\n　　8、服从公司领导统一安排的其他工作。\n　　二、员工行为规范及安全管理规范\n　　员工行为规范\n　　为了实现公司目标、维护公司利益、履行公司职责、严守职业道德，从思想认识到日常行为应遵守的职业纪律。员工的一言一行，一举一动，是公司形象的再现。不断提高员工的自身素质，规范员工行为是公司文化建设的切入点。\n　　1、遵纪守法，遵守国家法律法规、遵守公司规章制度，做到令行禁止。\n　　2、端正仪容仪表、讲究个人卫生、礼貌待人、使用文明用语。\n　　3、维护企业形象、确立大局意识、建立诚信观念，严格要求自己。\n　　4、树立良好的服务意识、提高工作效率。\n　　5、发扬团队精神，密切配合，同事之间相互尊重，团结协作。\n　　6、上班时间以工作为中心，不做与工作无关的事情。\n　　7、服从领导，听从指挥，努力工作，讲求效率，按时完成各项任务。\n　　8、维护公司信誉，保守公司商业秘密。\n　　安全管理规范\n　　1、执行“谁主管谁负责，谁在岗谁负责，谁操作谁负责”的安全岗位责任制。\n　　2、严禁在“禁止吸烟”的公司区域内吸烟。\n　　3、严禁私自安装各种电器设备，乱架电线。\n　　4、不准擅自动用、拆卸消防设备和消防工具；定期学习和使用灭火器材和消防设备。\n　　5、未得到工程部门认可，任何人不得在电缆井内施工。\n　　6、发现火情，立即拨打公司和消防报警电话，报警时讲清火灾具体地点、燃烧物品、火势大小、报警人姓名和岗位；关闭着火现场的所有门窗，切断电源；如火势较大，积极协助指挥人员疏导现场人员从安全通道撤离，严禁使用电梯，服从指挥。　　\n第三章管理制度\n　　第一节劳动人事管理制度\n　　员工招聘制度\n　　一、招聘条件：\n　　1、年龄在18周岁以上，50周岁以下\n　　2、管理岗位人员要求大专以上学历（含大专）\n　　3、其他岗位人员要求初中以上学历，同时具备其岗位所需的技能；保安保洁等岗位根据情况年龄可放宽至55周岁。\n　　二、员工聘用程序与审批\n　　1、应聘人员应如实填写登记表、并备有关证件复印件，经资格审查、公司领导审批后办理录用手续。\n　　2、员工到职两个月为试用期，工作表现特别优秀者，可缩短试用期。保安保洁员工试用期为一个月。试用期中的员工，如发现不符合岗位任职条件或违反公司管理规定者，公司有权随时停止试用并办理解聘手续。\n　　3、员工服务工龄，自试用之日起计算。\n　　4、员工试用期满合格者办理转正手续，并签订劳动合同。', '', '', '物业人员', '2026-03-18', '2029-03-22', 1, NULL, 'draft', 21, '梁经理', '2026-03-19 22:43:05', NULL, NULL, '2026-03-19 22:43:05');
INSERT INTO `regulation` VALUES (8, 'WY-KF-0004', '维修服务回访管理制度', '其他', '　　一. 维修回访由经理、主管、维修负责人担任。其中：经理回访率不低于10%；主管回访率不低于30%；维修负责人回访率不低于60%。\n　　二. 回访时间安排在维修后一星期之内。其中：安全设施维修两天内回访；漏水项目维修三天内回访。\n　　三. 回访内容：\n　　1. 实地查看维修项目。\n　　2. 向在维修现场的业主（住户）或家人了解维修人员服务情况。\n　　3. 征询改进意见。\n　　4. 核对收费情况。\n　　5. 请被回访人签名。\n　　四. 对回访中发现的问题，24小时内书面通知维修人员进行整改。\n', '', '', '业主和物业人员', '2026-03-18', '2029-03-15', 1, NULL, 'published', 21, '梁经理', '2026-03-19 23:12:52', NULL, NULL, '2026-03-19 23:13:01');
INSERT INTO `regulation` VALUES (9, 'WY-KF-0005', '业主（住户）回访及意见处理制度', '其他', '　　为加强物业管理处与广大住户（业主）的联系，使管理处各项工作置身于住户（业主）监督之中，从而集思广益，及时总结经验、教训，不断改进管理工作，提高服务质量，特制定对住户回访制度。　　\n一. 回访要求\n　　1. 物业管理处正、副主任把对住户（业主）的回访列入职责范围，并落实到每年的工作计划和总结评比中。\n　　2. 回访时，虚心听取意见，诚恳接受批评，采纳合理化建议，作好回访记录。\n　　3. 回访中，对住户（业主）的询问、意见，如不能当即答复，应告知预约时间回复。\n　　4. 回访后对反馈的意见、要求、建议、投诉，及时逐条整理综合、研究、妥善解决，重大问题向公司请示解决。对住户（业主）反映的问题，做到件件有着落，事事有回音。回访处理率达100%，投诉率力争控制在1%以下。　　\n二. 回访时间及形式\n　　1. 牧业管理处办公室主任每年登门回访1~2次。\n　　2. 物管员按区域范围分工，每月回访1次。\n　　3. 每季度召开一次楼长会，征求意见。\n　　4. 利用节日庆祝活动、社区文化活动、公关活动等形式广泛听取住户反映。\n　　5. 有针对性地对住户（业主）作专题调查，听取意见。\n　　6. 物业管理处设投诉信箱，投诉电话，由专人接收，交办公室主任及时处理。\n　　7. 随时热情接待来访，作好登记。\n', '', '', '业主和物业人员', '2026-03-19', '2028-03-08', 1, NULL, 'draft', 21, '梁经理', '2026-03-19 23:13:40', NULL, NULL, '2026-03-19 23:13:40');
INSERT INTO `regulation` VALUES (10, 'WY-CW-0006', '酬金制模式下物业管理财务监管制度', '其他', '1.0目的:\n　　根据物业管理服务委托合同的规定,为了明确酬金制物业管理运作模式下与委托物业管理企业的双方权利义务,有效监管委托物业管理企业财务经营状况,维护公司经济利益。　　\n2.0范围\n　　适用于酬金制物业管理运作模式下,xx集团对委托物业管理企业的财务监管工作。　　\n3.0职责\n　　3.1项目委托的物业管理公司(以下简称物业公司)负责项目物业管理具体经营、财务管理、资产管理的实际工作,以及经营收入管理和成本控制工作;\n　　3.2置业公司客户服务中心(以下简称客户服务中心)负责与物业公司对接工作,以及费用结算、成本审核、物资采购初审、财务核查协助、资产管理监督工作;\n　　3.3置业公司财务部(以下简称地产财务部)负责对物业公司财务管理、资产管理的监管和审核工作;\n　　3.4集团资产运营部负责上述工作的监督与指导工作。　　\n4.0程序\n　　4.1物业管理经营收入监管\n　　4.1.1物业管理经营收入定义与范围\n　　4.1.1.1物业管理经营收入是指物业公司在本项目上所有业务经营收入款项,包括主营业务经营收入和其他业务经营收入;\n　　4.1.1.2主营业务经营收入包括为物业管理服务费和停车服务费;\n　　4.1.1.3其他业务经营收入包括为装修管理费、垃圾清运费、场租费、业务分成、维修服务费、有偿服务费等其他业务收入;\n　　4.1.1.4所有主营业务经营收入和其他业务经营收入必须入帐,录入财务软件、开具地产财务部指定的统一票据,列入监管范围。\n　　4.1.2财务票据监控\n　　4.1.2.1物业公司收取所有的物业管理经营收入款项(包括主营业务经营收入和其他业务经营收入),都必须向客户开具地产财务部指定统一的《xx大湖之都物业管理收款收据》(以下简称物业管理财务收据),物业管理财务票据一式三联,一联给客户、一联物业公司存档、一联地产财务部存档;\n　　4.1.2.2物业管理财务收据由地产财务部负责设计、印刷和管理;\n　　4.1.2.3物业公司财务会计在每月3日前到地产财务部核销上月的物业管理财务收据,和领取本月物业管理财务收据。\n　　4.1.2.4物业公司在领取本月物业管理财务收据,需要在地产财务部登记物业管理财务收据的起始编号;\n　　4.1.2.5物业公司在核销物业管理财务收据时,需向地产财务部提交《xx大湖之都月份物业管理收入报表》,和当月使用完的全部物业管理财务票据第一联存根。客户服务中心根据月份报表和财务收存根进行审核物业公司当月经营收入数据的真实性,客户服务中心审核无误后提交地产财务部复核;\n　　4.1.2.6物业公司在使用《xx大湖之都物业管理收款收据》,必须按顺序和规范填写,如填写错误必须收回客户联,三联一起核销;不得有跳页、涂改、开空白页。自制和伪造财务票据、以及收款不开票据的行为,一经发现将根据《xx大湖之都前期物业管理服务委托合同》的约定,每发生一次将给物业公司处于人民币1万元的罚款;\n　　4.1.3物业管理软件监控\n　　4.1.3.1为便于财务数据的及时监控,将安装物业管理软件,在地产财务部、客户服务中心、物业公司各设置一个管理登陆平台;\n　　4.1.3.2物业公司收取所有的物业管理经营收入款项(包括主营业务经营收入和其他业务经营收入),必须及时将财务数据录入物业管理软件的数据库中;\n　　4.1.3.3客户服务中心在每月4日前将根据物业管理软件的数据统计,汇总和统计物业公司上月度的经营收入数据,并打印一份《物业公司月份经营收入数据汇总报表》(物业管理软件打印稿)。\n　　4.1.3.4客户服务中心根据《物业公司月份经营收入数据汇总报表》(物业管理软件打印稿),对照物业公司提交的《xx大湖之都月份物业管理收入报表》,和当月使用完的全部物业管理财务票据第一联存根,审核无误后提交地产财务部复核;\n　　4.1.3.5物业公司必须保证所有物业管理经营收入款项数据原则上在48小时内(停电、电脑或系统故障原因除外)录入物业管理软件的数据库中。如月度审核中发现收费不录入软件的行为,根据《xx大湖之都前期物业管理服务委托合同》的约定将每次处于物业公司人民币1万元罚款。\n　　4.1.4物业管理经营收入数据确认:地产财务部根据客户服务中心提交审核过的《物业公司月份经营收入数据汇总报表》(物业管理软件打印稿)、《xx大湖之都月份物业管理收入报表》、当月使用完的全部物业管理财务票据第一联存根,进行复核,复核无误后,在《xx大湖之都月份物业管理收入报表》签字确认,确认后的数据将成为物业公司当月的最终经营收入数据。\n　　4.2物业管理经营成本监管\n　　4.2.1物业管理经营成本定义与范围\n　　4.2.1.1物业管理成本包括人员物业管理人工成本和物业管理物料成本;\n　　4.2.1.2物业管理人工成本包括:工资、福利、加班费、社会保险;\n　　4.2.1.3物业管理物料成本包括:办公用品、维修材料、清洁材料、绿化材料等法律法规规定内的成本支出;\n　　4.2.1.4单价在500元以上的物资作为资产范围,按照资产管理监控程序处理;\n　　4.2.2物业管理人工成本监控\n　　4.2.2.1物业公司必须在每月3日前提交上月的《xx月份考勤汇总表》和《xx月份人工成本报表》(报表中必须有工资、福利、加班费以及社会保险金额);\n　　4.2.2.2客户服务中心对上述报表和项目实际人员状况进行核实和审核,审核无误后将提交地产财务部进行复核;\n　　4.2.2.3物业公司的人员编制、工资、福利和社会保险标准必须按照万厦居业的《xx大湖之都物业管理投标文件》中的标准执行,超出此标准的部分将不予以确认,超编制部分人员需提前报批;\n　　4.2.2.4地产财务部和客户服务中心在审核物业公司的人工成本时,如发现物业公司存在虚报人员考勤、工资、福利和社会保险的行为,将有权拒绝支付物业公司当月亏损补贴和酬金,并追究物业公司的违约责任和扣罚酬金;\n　　4.2.2.5物业管理人工成本数据确认:地产财务部对客户服务中心提交的审核合格的《xx月份人工成本报表》进行复核,复核无误后在报表上签字确认,确认后的数据将成为物业公司当月的最终人工成本数据;\n　　4.2.3物业管理物料成本监控\n　　4.2.3.1物业公司在每月28日前向客户服务中心提交下月的《xx月份物资采购报表》(报表中必须注明采购物资名称、规格、数量、用途、单价、总计金额)进行审核。客户服务中心根据项目实际情况审核所采购物资的合理性、必要性和真实性;\n　　4.2.3.2物业公司需每个季度向客户服务中心和地产财务部提交一份《xx大湖之都季度常用物资采购价格明细表》,以供价格参考。客户服务中心和地产财务部有权对价格表中的价格进行市场调查,如发现物业公司存在虚报价格的行为,将有权拒绝支付该项费用,并追究物业公司的违约责任和扣罚酬金;\n　　4.2.3.3客户服务中心审核过程中如发现,如发现《xx月份物资采购报表》的物资单价,超过《xx大湖之都季度常用物资采购价格明细表》单价的10%以上,将要求物业公司进行合理解释,否则将拒绝确认。客户服务中心审核合格后的《xx月份物资采购报表》,报一份给地产财务部复核;\n　　4.2.3.4物业管理物料成本数据确认:地产财务部根据客户服务中心审核后的《xx月份物资采购报表》,对照《xx大湖之都季度常用物资采购价格明细表》复核无误后,在《xx月份物资采购报表》签字确认,确认的数据将成为物业公司当月的最终物料成本数据;\n　　4.2.3.5物业公司根据审核合格后的《xx月份物资采购报表》实施采购。\n　　4.3酬金和亏损补贴支付\n　　4.3.1物业公司酬金提取标准按照《xx大湖之都前期物业管理服务委托合同》规定标准提取计算;\n　　4.3.2客户服务中心根据地产财务部签字确认后的《xx月份人工成本报表》和《xx月份物资采购报表》,计算当月管理酬金和亏损补贴,并签发《xx月份物业管理亏损补贴审批表》,报地产财务部和公司总经理审批,审批后在每月8日前凭物业公司开具的正式发票向物业公司指定帐号划拨补贴款项,;\n　　4.4资产管理监控\n　　4.4.1资产的定义和范围:是指单价在500元以上的物资;\n　　4.4.2资产购置申请:物业公司需要购置资产,需要提前7个工作日向客户服务中心提交《资产购置申请表》进行审批,客户服务中心审批后报地产财务部和公司总经理审批;\n　　4.4.3资产购置费用报销:物业公司凭审批合格后的《资产购置申请表》实施采购。报销时需要提交发票、入库单和《资产购置申请表》进行报销,属于前期开办费范围以内的资产购置报销由地产公司报销,前期开办费范围以外的资产购置报销由物业公司报销;\n　　4.4.4资产管理统计和监督:物业公司需要在每月8日前向客户服务中心提交《xx月份资产统计报表》,客户服务中心进行监督和核查,同时将此报表报地产综合部备案。\n　　4.4.5紧急采购和特殊情况采购:按照《xx大湖之都前期物业管理服务委托合同》规定执行。\n　　4.5集团监督:集团资产运营部对上述工作不定期进行检查和审核,发现存在违规和虚假行为,为集团内部员工责任的将按照集团行政管理制度进行处罚,为物业公司责任的将按照合同规定追究合同违约责任。　　\n5.0支持性文件及质量记录\n　　5.1《前期物业管理服务委托合同》\n　　5.2《物业管理收款收据》(地产财务部印制格式)\n　　5.3《zz月份物业管理收入报表》zr-kf-wy-01\n　　5.4《物业公司月份经营收入数据汇总报表》(物业管理软件打印稿)\n　　5.5《xx月份考勤汇总表》zr-kf-wy-02\n　　5.6《xx月份人工成本报表》zr-kf-wy-03\n　　5.7《xx月份物资采购报表》zr-kf-wy-04\n　　5.8《xx大湖之都季度常用物资采购价格明细表》zr-kf-wy-05\n　　5.9《xx月份物业管理亏损补贴审批表》zr-kf-wy-06\n　　5.10《资产购置申请表》zr-kf-wy-07\n\n', '', '', '物业人员', '2026-03-18', '2027-07-15', 1, NULL, 'published', 21, '梁经理', '2026-03-19 23:15:07', NULL, NULL, '2026-03-19 23:15:11');

-- ----------------------------
-- Table structure for repair_evaluation
-- ----------------------------
DROP TABLE IF EXISTS `repair_evaluation`;
CREATE TABLE `repair_evaluation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `repair_order_id` bigint NOT NULL COMMENT '报修订单ID（关联 repair_order 表）',
  `score` int NOT NULL COMMENT '评分（1-5分）',
  `comment` varchar(500) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '评价内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_repair_evaluation_repair_order`(`repair_order_id` ASC) USING BTREE,
  CONSTRAINT `fk_repair_evaluation_repair_order` FOREIGN KEY (`repair_order_id`) REFERENCES `repair_order` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '报修评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_evaluation
-- ----------------------------

-- ----------------------------
-- Table structure for repair_order
-- ----------------------------
DROP TABLE IF EXISTS `repair_order`;
CREATE TABLE `repair_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `repair_order_no` varchar(30) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '报修单号（BX+ 年月日时分秒 +4 位随机数）',
  `owner_id` bigint NOT NULL COMMENT '业主ID（关联 owner 表）',
  `repair_type_id` bigint NOT NULL COMMENT '报修类型ID（关联 repair_type 表）',
  `title` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '报修标题',
  `content` text CHARACTER SET gb2312 COLLATE gb2312_bin NULL COMMENT '报修内容',
  `address` varchar(200) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '详细地址',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-待审核/1-已派单/2-维修中/3-已完成/4-已取消/5-待验收/6-已验收）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '维修人员ID（关联 user 表）',
  `handler_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '维修人员姓名',
  `handler_phone` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '维修人员联系方式',
  `expected_visit_time` datetime NULL DEFAULT NULL COMMENT '预计上门时间',
  `assign_remark` varchar(500) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '派单备注',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '完成时间',
  `evaluation_score` int NULL DEFAULT NULL COMMENT '评价评分（1-5 分）',
  `evaluation_comment` varchar(500) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '评价内容',
  `evaluation_time` datetime NULL DEFAULT NULL COMMENT '评价时间',
  `remark` varchar(500) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '备注',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_repair_order_owner`(`owner_id` ASC) USING BTREE,
  INDEX `fk_repair_order_repair_type`(`repair_type_id` ASC) USING BTREE,
  INDEX `fk_repair_order_user`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_repair_order_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_repair_order_repair_type` FOREIGN KEY (`repair_type_id`) REFERENCES `repair_type` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_repair_order_user` FOREIGN KEY (`handler_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '报修订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_order
-- ----------------------------
INSERT INTO `repair_order` VALUES (29, 'BX202604021809043220', 12, 11, '卫生间漏水维修', '卫生间天花板漏水严重，需要紧急处理', '1栋-2605', 0, '2026-04-02 18:09:04', 2, '李师傅', '13800138002', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (30, 'BX202604021809323539', 12, 4, '电梯故障报修', '电梯经常卡顿，存在安全隐患[派单备注：，预计上门时间：2026-04-05 00:00]', '1栋-2605', 2, '2026-04-20 18:09:32', NULL, '张师傅', '13800138001', '2026-04-03 09:24:17', NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (31, 'BX202604021810178037', 23, 10, '空调不制冷', '客厅空调不制冷，需要加氟', '1栋-1101', 0, '2026-04-02 18:10:17', NULL, '王师傅', '13800138003', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (32, 'BX202604021811109566', 23, 12, '下水道堵塞', '厨房下水道严重堵塞，无法排水[派单备注：，预计上门时间：2026-04-03 00:00]', '1栋-1101', 2, '2026-04-02 18:11:10', NULL, '张师傅', '13800138001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (33, 'BX202604021811453720', 24, 5, '门窗损坏维修', '阳台玻璃门把手损坏，无法锁闭[派单备注：，预计上门时间：2026-04-04 00:00]', '2栋-2003', 3, '2026-04-02 18:11:45', NULL, '张师傅', '13800138001', NULL, NULL, '2026-04-03 09:24:17', 5, '维修师傅很专业，很快就修好了门窗把手，现在开关顺畅多了。服务态度好，工作效率高，非常满意！', '2026-04-03 09:24:17', NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (34, 'BX202604021812039902', 24, 2, '电路故障', '卧室插座没电，需要检查线路', '2栋-2003', 0, '2026-04-02 18:12:03', 4, '赵师傅', '13800138004', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (35, 'BX202604021813288349', 25, 6, '墙面修补', '客厅墙面开裂，需要修补', '3栋-1505', 1, '2026-04-02 18:13:28', 4, '赵师傅', '13800138004', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (36, 'BX202604021813532040', 25, 2, '灯具维修', '卫生间吸顶灯损坏，需要更换[派单备注：，预计上门时间：2026-04-04 00:00]', '3栋-1505', 5, '2026-04-02 18:13:53', NULL, '张师傅', '13800138001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (37, 'BX202604021814265155', 12, 2, '热水器故障', '热水器无法点火[派单备注：，预计上门时间：2026-04-04 00:00]', '1栋-2605', 5, '2026-04-02 18:14:26', NULL, '张师傅', '13800138001', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (38, 'BX202604021815169657', 12, 6, '地板起翘', '卧室木地板起翘严重[驳回原因：工程质量问题，属装修公司负责]', '1栋-2605', 4, '2026-04-02 18:15:16', 4, '赵师傅', '13800138004', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (39, 'BX202604031035523865', 24, 11, '卫生间漏水维修', '卫生间天花板漏水严重，已经渗透到楼下邻居家，需要紧急处理。漏水位置在洗手台上方，请师傅尽快上门查看。', '2栋-2003', 0, '2026-04-03 10:35:53', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (43, 'BX202604010000001', 12, 4, '电梯异响', '电梯运行时发出异响', '1栋-2605', 6, '2026-03-25 10:00:00', NULL, '张师傅', '13800138001', '2026-03-22 09:24:17', NULL, '2026-03-26 14:00:00', 3, '维修师傅很专业，很快就修好了门窗把手，现在开关顺畅多了。服务态度好，工作效率高，非常满意！', '2026-04-01 09:24:17', NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (44, 'BX202604010000002', 12, 4, '电梯停运', '电梯突然停止运行', '1栋-2605', 6, '2026-03-28 09:00:00', NULL, '张师傅', '13800138001', '2026-03-26 09:24:17', NULL, '2026-03-29 11:00:00', 4, '维修师傅很专业，很快就修好了门窗把手，现在开关顺畅多了。服务态度好，工作效率高，非常满意！', '2026-04-02 09:24:17', NULL, '2026-04-28 17:59:14');
INSERT INTO `repair_order` VALUES (45, 'BX202604010000003', 12, 4, '电梯按键失灵', '5楼按键无反应', '1栋-2605', 6, '2026-03-30 14:00:00', NULL, '张师傅', '13800138001', '2026-03-29 09:24:17', NULL, '2026-03-31 16:00:00', 5, '维修师傅很专业，很快就修好了门窗把手，现在开关顺畅多了。服务态度好，工作效率高，非常满意！', '2026-04-03 09:24:17', NULL, '2026-04-28 17:59:14');

-- ----------------------------
-- Table structure for repair_type
-- ----------------------------
DROP TABLE IF EXISTS `repair_type`;
CREATE TABLE `repair_type`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `type_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '报修类型名称（水电维修/家电维修/公共区域维修）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '报修类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of repair_type
-- ----------------------------
INSERT INTO `repair_type` VALUES (1, '自来水管道维修');
INSERT INTO `repair_type` VALUES (2, '电路故障维修');
INSERT INTO `repair_type` VALUES (3, '马桶堵塞维修');
INSERT INTO `repair_type` VALUES (4, '电梯故障维修');
INSERT INTO `repair_type` VALUES (5, '门窗损坏维修');
INSERT INTO `repair_type` VALUES (6, '墙面维修');
INSERT INTO `repair_type` VALUES (7, '燃气管道检修');
INSERT INTO `repair_type` VALUES (8, '公共照明故障');
INSERT INTO `repair_type` VALUES (9, '门禁系统维修');
INSERT INTO `repair_type` VALUES (10, '空调故障维修');
INSERT INTO `repair_type` VALUES (11, '卫生间故障维修');
INSERT INTO `repair_type` VALUES (12, '厨房故障维修');

-- ----------------------------
-- Table structure for staff
-- ----------------------------
DROP TABLE IF EXISTS `staff`;
CREATE TABLE `staff`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户 ID（关联 User 表）',
  `staff_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工号（唯一标识）',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号',
  `status` int NULL DEFAULT 1 COMMENT '状态：0-离职，1-在职，2-试用期',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_staff_no`(`staff_no` ASC) USING BTREE COMMENT '工号唯一索引',
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE COMMENT '用户 ID 索引'
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物业人员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of staff
-- ----------------------------
INSERT INTO `staff` VALUES (1, 26, 'WY20260001', '110101199001011234', 1, '2026-04-14 20:08:56', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (2, 22, 'WY20260002', '110101199102022345', 1, '2026-04-14 20:09:57', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (3, 20, 'WY20260003', '110101199203033456', 1, '2026-04-14 20:10:06', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (4, 11, 'WY20260004', '110101199304044567', 1, '2026-04-14 20:10:22', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (5, 8, 'WY20260005', '110101199405055678', 1, '2026-04-14 20:10:36', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (6, 5, 'WY20260006', '110101199506066789', 1, '2026-04-14 20:10:51', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (7, 3, 'WY20260007', '110101199607077890', 1, '2026-04-14 20:11:11', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (8, 1, 'WY20260008', '110101199708088901', 1, '2026-04-14 20:11:19', '2026-04-28 10:18:35', NULL);
INSERT INTO `staff` VALUES (9, 32, 'WY20260009', '110101199708088902', 1, '2026-04-28 19:02:21', '2026-04-28 19:02:35', NULL);
INSERT INTO `staff` VALUES (10, 35, 'WY20260010', '110101199708088903', 1, '2026-04-28 23:45:46', '2026-04-28 23:45:55', NULL);

-- ----------------------------
-- Table structure for suggestion
-- ----------------------------
DROP TABLE IF EXISTS `suggestion`;
CREATE TABLE `suggestion`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `user_id` bigint NOT NULL COMMENT '用户 ID（关联 user 表）',
  `owner_id` bigint NOT NULL COMMENT '业主 ID（关联 owner 表）',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '意见类型：complaint-投诉/suggestion-建议/repair-报修/consultation-咨询',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '意见标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '意见内容',
  `images` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图片路径（多张用逗号分隔）',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '处理状态：pending-待处理/processing-处理中/processed-已处理/replied-已回复',
  `reply_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回复内容',
  `reply_user_id` bigint NULL DEFAULT NULL COMMENT '回复人 ID（物业人员）',
  `reply_time` datetime NULL DEFAULT NULL COMMENT '回复时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_suggestion_user`(`user_id` ASC) USING BTREE,
  INDEX `fk_suggestion_owner`(`owner_id` ASC) USING BTREE,
  INDEX `idx_suggestion_type`(`type` ASC) USING BTREE,
  INDEX `idx_suggestion_status`(`status` ASC) USING BTREE,
  INDEX `idx_suggestion_create_time`(`create_time` ASC) USING BTREE,
  CONSTRAINT `fk_suggestion_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_suggestion_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '业主意见反馈表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of suggestion
-- ----------------------------
INSERT INTO `suggestion` VALUES (5, 12, 12, 'complaint', '小区停车管理混乱', '地下车库经常有外来车辆占用业主车位', '', '13536214651', 'replied', '已收到您的反馈，我们将加强车库巡逻和访客管理', 11, '2026-04-03 16:52:49', '2026-04-03 16:46:20', '2026-04-23 23:50:34');
INSERT INTO `suggestion` VALUES (6, 13, 13, 'suggestion', '建议增加快递柜', '小区入住率越来越高，建议在每栋楼附近增加快递柜，方便业主收取快递。', '', '13800138002', 'replied', '感谢建议，已联系快递公司考察选址。', 11, '2026-04-03 16:53:36', '2026-04-03 16:46:53', '2026-04-03 16:53:36');
INSERT INTO `suggestion` VALUES (7, 14, 14, 'repair', '电梯故障频繁', '3 栋 2 单元电梯最近经常出现故障，运行时有异响，希望尽快检修。', '', '13800138003', 'processing', NULL, NULL, NULL, '2026-04-03 16:47:50', '2026-04-03 16:54:29');
INSERT INTO `suggestion` VALUES (8, 14, 14, 'consultation', '物业费缴纳方式咨询', '请问物业费可以通过手机支付吗？有没有优惠措施？', '', '13800138004', 'replied', '可通过微信公众号缴纳，年缴享受 98 折优惠。', 11, '2026-04-03 16:54:44', '2026-04-03 16:48:20', '2026-04-03 16:54:44');
INSERT INTO `suggestion` VALUES (9, 15, 15, 'complaint', '楼道卫生清理不及时', '5 栋的楼道已经一周没有打扫了，垃圾堆积严重，影响居住环境。', '', '13800138005', 'replied', '已安排保洁人员立即清理，后续会加强检查。', 11, '2026-04-03 16:54:55', '2026-04-03 16:48:56', '2026-04-03 16:54:55');
INSERT INTO `suggestion` VALUES (10, 15, 15, 'suggestion', '建议增设儿童游乐设施', '小区儿童较多，建议在中心花园增设儿童游乐设施，如滑梯、秋千等。', '', '13800138006', 'pending', NULL, NULL, NULL, '2026-04-03 16:49:14', '2026-04-03 16:49:14');
INSERT INTO `suggestion` VALUES (11, 15, 15, 'repair', '公共区域照明损坏', '2 栋楼下的大堂灯坏了三盏，晚上很黑，存在安全隐患。', '', '13800138007', 'processing', '已安排维修人员更换，预计今晚完成。', 11, '2026-04-03 16:56:59', '2026-04-03 16:49:39', '2026-04-03 16:57:01');
INSERT INTO `suggestion` VALUES (12, 16, 16, 'consultation', '装修押金退还流程', '装修已完成，请问押金退还需要什么手续？多久能到账？', '', '13800138008', 'replied', '需携带验收单和收据到物业办理，7 个工作日内退还。', 11, '2026-04-03 16:57:45', '2026-04-03 16:50:20', '2026-04-03 16:57:45');
INSERT INTO `suggestion` VALUES (13, 16, 16, 'complaint', '噪音扰民问题严重', '近期有业主在休息时间装修，电钻声音严重影响休息，希望物业制止。', '', '13800138009', 'pending', NULL, NULL, NULL, '2026-04-03 16:50:37', '2026-04-03 16:50:37');
INSERT INTO `suggestion` VALUES (14, 16, 16, 'suggestion', '建议增加监控摄像头', '小区北门附近没有监控，经常有电动车丢失，建议安装监控摄像头。', '', '13800138010', 'pending', NULL, NULL, NULL, '2026-04-03 16:50:56', '2026-04-03 16:50:56');
INSERT INTO `suggestion` VALUES (15, 17, 17, 'repair', '消防通道被占用', '1 栋楼下的消防通道长期被电动车占用，存在严重消防安全隐患。', '', '13800138011', 'pending', NULL, NULL, NULL, '2026-04-03 16:51:35', '2026-04-03 16:51:35');
INSERT INTO `suggestion` VALUES (16, 12, 12, 'consultation', '居住证办理咨询', '外来租户如何办理居住证？需要物业提供什么证明材料？', '', '13536214651', 'pending', NULL, NULL, NULL, '2026-04-03 16:51:52', '2026-04-23 23:49:11');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `username` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '密码（加密存储）',
  `role` int NOT NULL COMMENT '角色（1-物业管理人员/2-业主）',
  `name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '姓名',
  `phone` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NULL DEFAULT NULL COMMENT '手机号',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态（0-禁用/1-正常）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (2, '李十四', '$2a$10$1234567890abcdefabcdefabcdefabcdefabcd', 2, '李十四', '13900139002', 1, '2025-01-12 10:20:15');
INSERT INTO `user` VALUES (4, 'owner002', '$2a$10$abcdef1234567890abcdefabcdefabcdefabcd', 2, '赵六', '13600136004', 1, '2025-01-18 08:45:10');
INSERT INTO `user` VALUES (6, 'sunba', '$2a$10$11223344556677889900aabbccddeeffaabb', 2, '孙八', '13400130006', 1, '2025-01-22 11:10:30');
INSERT INTO `user` VALUES (7, 'owner004', '$2a$10$99887766554433221100zzxxyywwvvuuttss', 2, '周九', '13300133007', 1, '2025-01-25 15:50:15');
INSERT INTO `user` VALUES (9, 'owner005', '$2a$10$zzxxyywwvvuutt99887766554433221100qq', 2, '郑十一', '13100131009', 1, '2025-02-01 13:40:25');
INSERT INTO `user` VALUES (10, 'owner07', '$2a$10$qqwweerrttyyuuiiooppaassddffgghhjjkkll', 2, '冯十二', '13000130010', 1, '2025-02-05 17:15:10');
INSERT INTO `user` VALUES (11, 'admin06', '$2a$10$uy1X5055qyX1hxXp7DDf7uWoBb3qZxAV9Ink3iWzaw4gSTdfcgNXq', 1, '王强', '13536214652', 1, '2026-02-14 15:10:03');
INSERT INTO `user` VALUES (12, 'owner06', '$2a$10$PtH3db1a89YA7m5QH4uJsOegF9JLttoATJ5wk.OAL7LurE/Ty/TD6', 2, '蒋十五', '13536214653', 1, '2026-02-14 15:12:08');
INSERT INTO `user` VALUES (13, '张十三', '$2a$10$2saq9lG9Fws8WWSpUOc0sOK5065QCwRgthfB5QoRvbzlYq1gG1pTi', 2, '张十三', '13812345678', 1, '2026-03-09 16:18:13');
INSERT INTO `user` VALUES (14, '李十七', '$2a$10$ZO0MgvMzaqVEzhcJXg7GjOcBoKnF6p9UK5Hf/HZDnUrWmKOEqOAuO', 2, '李十七', '13987654321', 1, '2026-03-09 16:25:33');
INSERT INTO `user` VALUES (15, '胡十八', '$2a$10$E/W6m1MKioReJBrZbM2bZO3r5hQE.IErl5HzGDP1lZQyoej2l7o.6', 2, '胡十八', '13611112222', 1, '2026-03-09 16:28:12');
INSERT INTO `user` VALUES (16, 'zhaoliu', '$2a$10$6EyNufOJYscEWM8XM1xmWukbJeYbS7SjukdqycARk3j3swu1pC4IC', 2, '赵六', '13522223333', 1, '2026-03-09 16:31:36');
INSERT INTO `user` VALUES (17, 'sunqi', '$2a$10$DEY1V2qNvD/1NcZuIPsjleEbSOquBtJQ7889UG5ArgW.blskXCaoG', 2, '孙七', '13733334445', 1, '2026-03-09 16:34:03');
INSERT INTO `user` VALUES (20, 'admin07', '$2a$10$WjTmGHopsMa.vX60e/2ZheO0ZWdCSjVqio4PT8wrH1DakL9BiJK5u', 1, '李娜', '13422223333', 1, '2026-03-18 19:51:15');
INSERT INTO `user` VALUES (21, '物业经理', '$2a$10$l79BxC5Qo2mmU31/jSSX5.bjvTiwmyWkYG6eF.FKN1ky52mdIwO8m', 3, '梁经理', '13536218889', 1, '2026-03-18 19:52:17');
INSERT INTO `user` VALUES (22, 'admin08', '$2a$10$Mrh/T6hmHEQY0MJGGTbw9ONCPVVvCZkNk6AWT1CVM8BsBKygYuijG', 1, '张伟', '13422223333', 1, '2026-03-18 19:52:45');
INSERT INTO `user` VALUES (26, '13500001001', '$2a$10$shM0SqhXUNOQkJEDuX8AQOkaULmnM2utPa7JdgjpsfCFY0g9jwjx6', 2, '赵七', '13500001001', 1, '2026-04-12 20:11:00');
INSERT INTO `user` VALUES (27, '13700137004', '$2a$10$VueG5Ma5NkmH4OlGx5Geg.M3nPcRqDLBlu76sw.Z7NUnfNnx0qVx6', 2, '杨雪', '13700137004', 1, '2026-04-28 09:49:05');
INSERT INTO `user` VALUES (28, '13500135006', '$2a$10$yVfGYSyklgKIEr2PbYlzHOV3vvsqmYEbmYGPreknR0ksm4yUStDCW', 2, '陈杰', '13500135006', 1, '2026-04-28 09:56:04');
INSERT INTO `user` VALUES (29, '13200132081', '$2a$10$Q7cRtZMBgY2GptHommNKQuWz08oLNTIiuqjta0xlm8qWT6iqWB.6K', 2, '刘敏', '13200132081', 1, '2026-04-28 10:10:37');
INSERT INTO `user` VALUES (32, '张七', '$2a$10$14TXlH3pzO3u6ZhpSzHcWepSDWx8bFWJoOyUtTGB0XdEbBY07wUuy', 1, '张七', '13200132083', 1, '2026-04-28 19:02:20');
INSERT INTO `user` VALUES (34, '张久', '$2a$10$YDSiB1rSxlSJOVOQoB1.xuUIt7uxDkwnpiVDI0Ua3XRaHYrvDpbFW', 2, '张久', '13800138004', 1, '2026-04-28 23:35:58');
INSERT INTO `user` VALUES (36, '13800138003', '$2a$10$dwBc1BeQ383JYksmomectOfRYcRODdyRtj/UQbb.D3FRO20/oQeYG', 2, '黄凯', '13800138003', 1, '2026-04-29 04:02:17');

-- ----------------------------
-- Table structure for visitor_invite
-- ----------------------------
DROP TABLE IF EXISTS `visitor_invite`;
CREATE TABLE `visitor_invite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `owner_id` bigint NOT NULL COMMENT '业主ID（关联 owner 表）',
  `visitor_name` varchar(50) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '访客姓名',
  `visitor_phone` varchar(20) CHARACTER SET gb2312 COLLATE gb2312_bin NOT NULL COMMENT '访客电话',
  `visit_time` datetime NOT NULL COMMENT '预约时间',
  `qrcode` text CHARACTER SET gb2312 COLLATE gb2312_bin NULL COMMENT '二维码内容',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态（0-待核销/1-已核销/2-已过期）',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_visitor_invite_owner`(`owner_id` ASC) USING BTREE,
  CONSTRAINT `fk_visitor_invite_owner` FOREIGN KEY (`owner_id`) REFERENCES `owner` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '访客邀请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visitor_invite
-- ----------------------------
INSERT INTO `visitor_invite` VALUES (11, 13, '黄', '13566668888', '2026-03-07 00:00:00', '$2y$10$lBccQHRnVgMeI3TyuYuTEOIrxTaCASd5N7STUCwtFHdbO8Basus2y', 0, '2026-03-07 15:03:52', '2026-03-08 15:03:52', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (12, 13, '王', '13466668888', '2026-03-08 00:00:00', '$2y$10$GSiw5M1u.rF6oalQWumD5e0mTO25B7FIW8q_jdNuEvp1J_U9QAxhe', 1, '2026-03-07 15:04:23', '2026-03-08 15:04:23', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (13, 17, '李', '13266668888', '2026-03-05 00:00:00', '$2y$10$dpFMRaDSI.xdLC8I0FbQf.P3PlOqnKc0GVzLscPvPnv7YqkYDAHFS', 2, '2026-03-07 15:04:36', '2026-03-07 17:04:36', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (14, 12, '张伟', '13812345678', '2026-03-25 10:00:00', '$2y$10$ABC123DEF456GHI789JKL0', 0, '2026-03-23 09:00:00', '2026-03-26 10:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (15, 12, '李娜', '13923456789', '2026-03-24 12:30:00', '$2y$10$MNO234PQR567STU890VWX1', 1, '2026-03-23 10:00:00', '2026-03-25 14:30:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (16, 13, '王芳', '13634567890', '2026-03-20 09:00:00', '$2y$10$YZA345BCD678EFG901HIJ2', 2, '2026-03-18 11:00:00', '2026-03-21 09:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (17, 15, '刘强', '13745678901', '2026-03-26 09:00:00', '$2y$10$KLB456CDE789FGH012IJK3', 0, '2026-03-23 14:00:00', '2026-03-27 15:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (18, 13, '陈静', '13556789012', '2026-03-23 16:30:00', '$2y$10$LMN567DEF890GHI123JKL4', 1, '2026-03-22 09:30:00', '2026-03-24 16:30:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (19, 14, '杨帆', '13867890123', '2026-03-22 08:30:00', '$2y$10$OPQ678FGH901IJK234LMN5', 2, '2026-03-20 10:00:00', '2026-03-22 08:30:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (20, 14, '赵敏', '13978901234', '2026-03-27 11:00:00', '$2y$10$RST789GHI012JKL345MNO6', 0, '2026-03-23 15:00:00', '2026-03-28 11:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (21, 15, '孙杰', '13689012345', '2026-03-24 10:30:00', '$2y$10$TUV890HIJ123KLM456NOP7', 0, '2026-03-23 16:00:00', '2026-03-25 10:30:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (22, 16, '周婷', '13790123456', '2026-03-21 14:00:00', '$2y$10$VWX901IJK234LMN567OPQ8', 1, '2026-03-20 09:00:00', '2026-03-22 14:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (23, 16, '吴鹏', '13801234567', '2026-03-19 09:30:00', '$2y$10$YZA012JKL345MNO678PQR9', 2, '2026-03-17 11:00:00', '2026-03-20 09:30:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (24, 12, '郑霞', '13912345670', '2026-03-28 16:00:00', '$2y$10$BCD123KLM456NOP789QRS0', 0, '2026-03-23 17:00:00', '2026-03-29 16:00:00', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (25, 13, '黄', '13566668888', '2026-03-07 00:00:00', '$2y$10$lBccQHRnVgMeI3TyuYuTEOIrxTaCASd5N7STUCwtFHdbO8Basus2y', 0, '2026-03-07 15:03:52', '2026-03-08 15:03:52', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (26, 13, '王', '13466668888', '2026-03-08 00:00:00', '$2y$10$GSiw5M1u.rF6oalQWumD5e0mTO25B7FIW8q_jdNuEvp1J_U9QAxhe', 1, '2026-03-07 15:04:23', '2026-03-08 15:04:23', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (27, 17, '李', '13266668888', '2026-03-05 00:00:00', '$2y$10$dpFMRaDSI.xdLC8I0FbQf.P3PlOqnKc0GVzLscPvPnv7YqkYDAHFS', 2, '2026-03-07 15:04:36', '2026-03-07 17:04:36', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (28, 12, '郑霞', '13912345670', '2026-04-15 00:00:00', '$2y$10$baQ8iyg7cbhQC6oHHRnO..3UCU5pgjdNK7M4SbBotr_9xTcmeas0S', 0, '2026-04-14 12:15:50', '2026-04-15 12:15:50', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (29, 12, '郑霞', '13912345670', '2026-04-24 00:00:00', '', 0, '2026-04-22 16:36:12', '2026-04-23 16:36:12', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (30, 12, '郑霞', '13912345670', '2026-04-24 00:00:00', '', 0, '2026-04-22 16:36:21', '2026-04-23 16:36:21', '2026-04-28 11:37:08');
INSERT INTO `visitor_invite` VALUES (32, 12, '郑霞', '13912345670', '2026-04-29 00:00:00', 'eVRvbTM4UFp5ZmUxMFd4b05xZXVWRnhCRzdHRndoQ2YreHVMby8rcDlTMWF3VFVZS292YWZyS0R1eUpOWDNYbkRGYTEzQkFZd1ByUlMyWms3Rkk3a0FsdHdNRjY1R1F2LzFJYWhxT2JST1YrbHRmeUF5NXZaZUtlQ2dEaHRhdEhRR2VGdjRqOSt6VnZ1cGlrQzBYT2R3M21aa0s4TGlGM3lrZTJibStqNS92Y0RraEU1VWZMcXdXai83QkIyejlLamZreUJibVdlSklYeDlld2lGcWM2V0lMd0dhUXZUdTBIOEp5OHNVblI5WT18eVdvK2ZhcWtsUVJMdkZac2RqL0JCdk0ra1o5V0cwNUtSVFVHR0hQMUVyRT0=', 1, '2026-04-28 18:08:17', '2026-04-29 18:08:17', '2026-04-28 18:08:17');
INSERT INTO `visitor_invite` VALUES (33, 12, '郑霞', '13912345670', '2026-04-28 00:00:00', 'UEIrWkpCVENkVkZTNS9lcWovWjhBbHhCRzdHRndoQ2YreHVMby8rcDlTMWF3VFVZS292YWZyS0R1eUpOWDNYbkRGYTEzQkFZd1ByUlMyWms3Rkk3a0x1ckNQNDhrNVNJV3NEMnIwSVEzT3QrbHRmeUF5NXZaZUtlQ2dEaHRhdEh5WXZJSXJ3YkFOZXBQcld3UEtHODV4VktOeWpaMXl3dG1lckpVZXJTRmVuY0RraEU1VWZMcXdXai83QkIyejlLamZreUJibVdlSklYeDlld2lGcWM2V0lMd0dhUXZUdTBIOEp5OHNVblI5WT18d2h4WnR5d1lRZy9hQWhaRllXNk9aYnFZVHh4N21HODlXRDlaYnRGODBFZz0=', 1, '2026-04-28 23:32:00', '2026-04-29 23:32:00', '2026-04-28 23:31:59');

-- ----------------------------
-- Table structure for visitor_record
-- ----------------------------
DROP TABLE IF EXISTS `visitor_record`;
CREATE TABLE `visitor_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `invite_id` bigint NOT NULL COMMENT '邀请ID（关联 visitor_invite 表）',
  `checkin_time` datetime NOT NULL COMMENT '核销时间（进入时间）',
  `checkout_time` datetime NULL DEFAULT NULL COMMENT '离开时间',
  `checker_id` bigint NOT NULL COMMENT '核销人员ID（关联 user 表）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_visitor_record_visitor_invite`(`invite_id` ASC) USING BTREE,
  INDEX `fk_visitor_record_user`(`checker_id` ASC) USING BTREE,
  CONSTRAINT `fk_visitor_record_user` FOREIGN KEY (`checker_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_visitor_record_visitor_invite` FOREIGN KEY (`invite_id`) REFERENCES `visitor_invite` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = gb2312 COLLATE = gb2312_bin COMMENT = '访客记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of visitor_record
-- ----------------------------
INSERT INTO `visitor_record` VALUES (17, 19, '2026-03-22 16:25:10', '2026-03-22 18:45:30', 11);

SET FOREIGN_KEY_CHECKS = 1;
