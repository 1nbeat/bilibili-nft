CREATE TABLE `bili_nft_item_attribute` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `item_id` BIGINT NOT NULL COMMENT '系列ID',
  `nft_id` VARCHAR(64) NOT NULL COMMENT '藏品ID',
  `trait_type` VARCHAR(128) NOT NULL COMMENT '属性名称',
  `trait_value` VARCHAR(255) NOT NULL COMMENT '属性值',
  `trait_count` INT DEFAULT NULL COMMENT '出现次数',
  `rarity_rate` DECIMAL(10,6) DEFAULT NULL COMMENT '稀有度，出现次数/系列总数',
  `crawl_time` DATETIME NOT NULL COMMENT '爬取时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_nft_trait` (`nft_id`, `trait_type`, `trait_value`),
  KEY `idx_item_trait` (`item_id`, `trait_type`, `trait_value`),
  KEY `idx_nft_id` (`nft_id`),
  KEY `idx_item_rarity` (`item_id`, `rarity_rate`),
  KEY `idx_crawl_time` (`crawl_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='B站数字藏品属性明细表';
