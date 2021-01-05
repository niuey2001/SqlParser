/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50721
Source Host           : localhost:3306
Source Database       : niuey

Target Server Type    : MYSQL
Target Server Version : 50721
File Encoding         : 65001

Date: 2021-01-05 16:31:14
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for dds_depends
-- ----------------------------
DROP TABLE IF EXISTS `dds_depends`;
CREATE TABLE `dds_depends` (
  `dds_name` varchar(200) DEFAULT NULL,
  `yl_tables` varchar(200) DEFAULT NULL,
  `czlx` varchar(255) DEFAULT NULL COMMENT '操作类型',
  `err` varchar(2000) DEFAULT NULL,
  `bz` char(1) DEFAULT NULL,
  KEY `depends_dds_name` (`dds_name`) USING BTREE,
  KEY `depends_dds_yltable` (`yl_tables`),
  KEY `depends_dds_czlx` (`czlx`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
