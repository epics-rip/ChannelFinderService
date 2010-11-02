USE `channelfinder`;

DROP TABLE IF EXISTS `value`;
DROP TABLE IF EXISTS `property`;
DROP TABLE IF EXISTS `channel`;
DROP VIEW IF EXISTS `prop_value`;

CREATE TABLE `channel` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(45) NOT NULL,
  `owner` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `channel_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `property` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(45) NOT NULL,
  `owner` varchar(45) NOT NULL,
  `is_tag` boolean NOT NULL default FALSE,
  PRIMARY KEY  (`id`),
  KEY `property_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `value` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `channel_id` int(10) unsigned NOT NULL,
  `property_id` int(10) unsigned NOT NULL,
  `value` varchar(45) default NULL,
  PRIMARY KEY  (`id`),
  KEY `value_channel` USING BTREE (`channel_id`),
  KEY `value_property` USING BTREE (`property_id`),
  CONSTRAINT `value_property` FOREIGN KEY (`property_id`) REFERENCES `property` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `value_channel` FOREIGN KEY (`channel_id`) REFERENCES `channel` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE VIEW `prop_value` AS
  SELECT
    `c`.`id` AS `channel_id`,
    `c`.`name` AS `channel`,
    `c`.`owner` AS `cowner`,
    `p`.`name` AS `property`,
    `p`.`owner` AS `powner`,
    `p`.`is_tag` AS `is_tag`,
    `v`.`value` AS `value`
  FROM
    (`channel` `c` LEFT JOIN `value` `v` on `c`.`id` = `v`.`channel_id`)
    JOIN `property` `p` ON `p`.`id` = `v`.`property_id`;
