/*
Navicat MySQL Data Transfer

Source Server         : Windows 8
Source Server Version : 80027
Source Host           : localhost:3306
Source Database       : daguai

Target Server Type    : MYSQL
Target Server Version : 80027
File Encoding         : 65001

Date: 2023-04-18 20:31:31
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
  `id` bigint NOT NULL COMMENT '主键',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `phone` varchar(255) NOT NULL COMMENT '手机号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES ('1', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13112312312');

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` bigint NOT NULL COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '分类id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '博客名称',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '博客内容',
  `image` varchar(255) DEFAULT NULL COMMENT '博客图片名称',
  `url` varchar(255) DEFAULT NULL COMMENT '活动链接',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除,0未删除,1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of blog
-- ----------------------------
INSERT INTO `blog` VALUES ('35601572327063555', '2', '12333', '313131', '', '333', '2023-04-06 22:32:17', '2023-04-06 22:32:17', '0');
INSERT INTO `blog` VALUES ('1630573378579795970', '1630487520522547202', '奥德赛test', '阿达瓦多', 'c8688fc2-4313-48c7-b3af-de7f209633e3.jpg', '234', '2023-02-28 22:19:23', '2023-04-06 11:26:04', '0');

-- ----------------------------
-- Table structure for blog_product
-- ----------------------------
DROP TABLE IF EXISTS `blog_product`;
CREATE TABLE `blog_product` (
  `id` bigint NOT NULL COMMENT '主键',
  `blog_id` bigint DEFAULT NULL COMMENT '博客id',
  `product_id` bigint DEFAULT NULL COMMENT '产品id',
  `name` varchar(255) DEFAULT NULL COMMENT '产品名称(冗余字段)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of blog_product
-- ----------------------------
INSERT INTO `blog_product` VALUES ('1643984979001413633', '35601572327063555', '1633117903110017025', '4');
INSERT INTO `blog_product` VALUES ('1643984979001413634', '35601572327063555', '1633117857652150274', '2');

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL COMMENT '分类id',
  `type` int NOT NULL COMMENT '类型,1为产品分类,2为博客分类',
  `name` varchar(255) NOT NULL COMMENT '分类名称',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除,0未删除,1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES ('1', '1', '萌宠', '2023-02-28 16:22:46', '2023-02-28 16:22:50', '0');
INSERT INTO `category` VALUES ('2', '2', '活动发布啊', '2023-02-28 16:23:11', '2023-04-06 11:24:18', '0');
INSERT INTO `category` VALUES ('1630487520522547202', '2', '杂谈', '2023-02-28 16:38:13', '2023-04-06 22:31:33', '0');
INSERT INTO `category` VALUES ('1630870771497988098', '1', '可爱的', '2023-03-01 18:01:07', '2023-04-05 21:55:54', '0');
INSERT INTO `category` VALUES ('1633836126562504706', '1', '奇奇怪怪的', '2023-03-09 22:24:22', '2023-03-09 22:24:22', '0');

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback` (
  `id` bigint NOT NULL COMMENT '主键',
  `question` varchar(255) DEFAULT NULL COMMENT '问题',
  `reply` varchar(255) DEFAULT NULL COMMENT '回复',
  `status` int DEFAULT '0' COMMENT '反馈状态,0未回复,1已回复',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of feedback
-- ----------------------------
INSERT INTO `feedback` VALUES ('1647835999863365634', '131231', null, '0', '2023-04-17 13:34:52', '2023-04-17 13:34:52', '0');
INSERT INTO `feedback` VALUES ('1647836009443155969', '123123123', '123', '1', '2023-04-17 13:34:55', '2023-04-17 14:10:36', '0');
INSERT INTO `feedback` VALUES ('1647844562321080321', '213', null, '0', '2023-04-17 14:08:54', '2023-04-17 14:08:54', '0');
INSERT INTO `feedback` VALUES ('1647844846812332033', 'test', null, '0', '2023-04-17 14:10:02', '2023-04-17 14:10:02', '0');

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL COMMENT '主键',
  `category_id` bigint NOT NULL COMMENT '产品分类id',
  `name` varchar(255) NOT NULL COMMENT '产品名称',
  `image` varchar(255) NOT NULL COMMENT '图片名称',
  `description` varchar(255) NOT NULL COMMENT '产品描述',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `is_delete` int DEFAULT '0' COMMENT '逻辑删除,0未删除,1已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ----------------------------
-- Records of product
-- ----------------------------
INSERT INTO `product` VALUES ('1633117832742178817', '1630870771497988098', '1', 'db5bb2b3-7361-4fa6-902f-ba32635043b0.jpg', '', '2023-03-07 22:50:08', '2023-03-07 22:50:08', '0');
INSERT INTO `product` VALUES ('1633117857652150274', '1', '2', '8d3c49a5-a1d7-4a11-88e0-691406cbc021.jpg', '', '2023-03-07 22:50:14', '2023-03-07 22:50:14', '0');
INSERT INTO `product` VALUES ('1633117878816608257', '1630870771497988098', '3', 'eabd641f-1fe5-4841-a7e5-24c539231f2c.jpg', '', '2023-03-07 22:50:19', '2023-03-07 22:50:19', '0');
INSERT INTO `product` VALUES ('1633117903110017025', '1', '4', '3cad63e1-d1dd-4a2d-9e9b-8ef20ad21628.jpg', '', '2023-03-07 22:50:25', '2023-03-07 22:50:25', '0');
INSERT INTO `product` VALUES ('1633117924572270594', '1630870771497988098', '5', '588bd7bf-63ba-41bc-8ad2-d068b038cfa7.jpg', '', '2023-03-07 22:50:30', '2023-03-07 22:50:30', '0');
INSERT INTO `product` VALUES ('1633117949050228737', '1', '6', 'c8688fc2-4313-48c7-b3af-de7f209633e3.jpg', '', '2023-03-07 22:50:36', '2023-03-07 22:50:36', '0');
INSERT INTO `product` VALUES ('1633449025304010754', '1', '驱蚊器', '204ee493-0f5f-4708-8832-a47b713b234b.jpg', '', '2023-03-08 20:46:10', '2023-03-08 20:46:10', '0');
INSERT INTO `product` VALUES ('1633449058665504770', '1630870771497988098', '驱蚊器翁', '11f3fcda-e518-4442-9880-802a913a495d.jpg', '', '2023-03-08 20:46:18', '2023-03-08 20:46:18', '0');
INSERT INTO `product` VALUES ('1633479201253498882', '1630870771497988098', '123123', '53e45f5e-b631-4581-96fa-5602775093ab.jpg', '', '2023-03-08 22:46:05', '2023-03-08 22:46:05', '0');
