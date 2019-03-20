CREATE TABLE `state_machine_task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `machine_code` varchar(48) DEFAULT NULL COMMENT '状态机唯一code',
  `machine_state` varchar(48) DEFAULT NULL COMMENT '状态机业务状态',
  `machine_type` varchar(48) DEFAULT NULL COMMENT '状态机业务类型，比如360、幼儿园等',
  `scan_status` varchar(48) DEFAULT NULL COMMENT '扫描状态''open'',''running'',''error''，''suspend''''close''',
  `transaction_id` varchar(128) DEFAULT NULL COMMENT '唯一key',
  `request_data` text COMMENT '请求参数',
  `response_data` mediumtext COMMENT 'task执行返回',
  `current_trytimes` int(11) DEFAULT '0' COMMENT '当前重试次数',
  `retry_times` int(11) DEFAULT '3' COMMENT '总重试次数',
  `next_run_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_scanstatus_next_run_time` (`scan_status`,`next_run_time`),
  KEY `idx_transaction_id` (`transaction_id`),
  KEY `idx_update_time_type` (`update_time`,`machine_type`,`scan_status`)
) ENGINE=InnoDB AUTO_INCREMENT=865 DEFAULT CHARSET=utf8mb4 COMMENT='状态机';

CREATE TABLE `state_machine_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `machine_code` varchar(48) DEFAULT NULL COMMENT '状态机code',
  `source` varchar(48) DEFAULT NULL COMMENT '源状态',
  `target` varchar(48) DEFAULT NULL COMMENT '目标状态',
  `event` varchar(48) DEFAULT NULL COMMENT '事件',
  `transition_result` varchar(48) DEFAULT NULL COMMENT '状态扭转结果',
  `response` mediumtext COMMENT '返回信息',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2879 DEFAULT CHARSET=utf8mb4;