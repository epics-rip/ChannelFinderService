DROP TABLE IF EXISTS `channel`;
CREATE TABLE `channel` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `name` varchar(45) NOT NULL,
  `owner` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `property`;
CREATE TABLE `property` (
  `id` int(10) unsigned NOT NULL auto_increment,
  `channel_id` int(10) unsigned NOT NULL,
  `property` varchar(45) NOT NULL,
  `value` varchar(45) default NULL,
  `owner` varchar(45) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `property_channel` (`channel_id`),
  KEY `property_name` (`property`),
  CONSTRAINT `property_channel` FOREIGN KEY (`channel_id`) REFERENCES `channel` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
