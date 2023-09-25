CREATE TABLE `order` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `sku_Id` bigint(20) NOT NULL COMMENT 'SkuId',
 `amount` bigint(20) DEFAULT NULL COMMENT '购买数量',
 `money` decimal(20,6) DEFAULT NULL COMMENT '购买金额',
 `pay_time` datetime DEFAULT NULL COMMENT '购买时间',
 `pay_status` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '支付状态',
 `user_id` bigint(20) NOT NULL COMMENT '购买人',
 `remark` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '备注',
 `del_flag` bigint(20) DEFAULT '0' COMMENT '删除标志（0代表存在 1代表删除）',
 `create_by` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '创建人',
 `create_time` datetime DEFAULT NULL COMMENT '创建时间',
 `update_by` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '更新人',
 `update_time` datetime DEFAULT NULL COMMENT '更新时间',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

-- 索引，思路：买家可以命中userId索引，卖家可以命中skuId索引；payTime索引,payStatus索引应对日期/状态搜索；不用组合索引是因为要考虑sql语句，所以暂时直接用普通索引。
KEY `sku_id_index` (`sku_Id`) USING BTREE COMMENT 'skuId索引',
KEY `user_id_index` (`user_id`) USING BTREE COMMENT 'userId索引',
KEY `pay_time_index` (`pay_time`) USING BTREE COMMENT 'payTime索引',
KEY `pay_status_index` (`pay_status`) USING BTREE COMMENT 'payStatus索引'

-- 分库,分表键：
-- 简单考虑可以用id%2的做法，分库database_0,database_1，分表同理，如order_0,order_1。
-- 也可以用中间件ShardingSphere，方便开发。分库时可以配置哪个字段，用什么算法来分(如InlineShardingAlgorithm,StandardShardingAlgorithm)；分表也可以自定义配置。

-- 历史订单删除：
-- 数据备份和归档：在执行删除操作之前，务必进行数据备份和归档。历史订单数据可能在未来需要进行审计、分析或法律合规方面使用。因此，在删除之前应该将这些数据进行备份，以便将来的访问和恢复。
-- 日志记录：在执行删除操作时，记录相应的日志，包括删除的数据量、时间戳等信息，以便后续追踪和审计。
-- 数据库约束和外键关系：确保删除历史订单数据不会违反数据库中的约束条件和外键关系。如果历史订单数据与其他数据表存在关联，需要考虑如何处理这些关联数据，以免引发数据一致性问题。
-- 删除策略：考虑使用何种删除策略。通常，可以根据订单的状态和时间来确定哪些订单可以删除。例如，已完成并且超过一定时间的订单可能可以被删除。
-- 定期任务：确定要保留多少时间的历史订单数据，将历史数据删除操作设置为定期任务，自动执行。这可以确保数据保持清洁，而不需要手动干预。
