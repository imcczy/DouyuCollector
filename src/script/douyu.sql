/*
Navicat MySQL Data Transfer

Source Server         : root@127.0.0.1
Source Server Version : 50719
Source Host           : 127.0.0.1:3306
Source Database       : douyu

Target Server Type    : MYSQL
Target Server Version : 50719
File Encoding         : 65001

Date: 2017-10-27 21:23:23
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `cate1_id` int(11) NOT NULL,
  `cate1_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cate1_short_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cate2_id` int(11) NOT NULL,
  `cate2_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `cate2_short_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pic` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `small_icon` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `update_time` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`cate1_id`,`cate2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for gift
-- ----------------------------
DROP TABLE IF EXISTS `gift`;
CREATE TABLE `gift` (
  `id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pc` double DEFAULT NULL,
  `gx` double DEFAULT NULL,
  `desc` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `intro` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mimg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `himg` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room` (
  `room_id` int(11) NOT NULL,
  `cate_id` int(11) DEFAULT NULL,
  `cate_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `room_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_weight` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fans_num` int(11) DEFAULT NULL,
  `room_thumb` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `update_time` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
