CREATE TABLE `bili_nft_series` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `item_id` BIGINT NOT NULL COMMENT '藏品系列ID',
  `item_name` VARCHAR(255) NOT NULL COMMENT '藏品系列名称',
  `issuer_name` VARCHAR(255) DEFAULT NULL COMMENT '发行方名称',
  `collection_total` INT DEFAULT NULL COMMENT '系列总数量',
  `is_private` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否私密',
  `preview_imgs` JSON DEFAULT NULL COMMENT '系列预览图JSON',
  `crawl_time` DATETIME NOT NULL COMMENT '爬取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_id` (`item_id`),
  KEY `idx_issuer_name` (`issuer_name`),
  KEY `idx_crawl_time` (`crawl_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='B站数字藏品系列表';

