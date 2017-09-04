SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";

DROP TABLE IF EXISTS `device`;
CREATE TABLE `device` (
	`deviceID` int(11) NOT NULL AUTO_INCREMENT,
	`title` tinytext,
	`name` tinytext NOT NULL,
	`type` tinytext,
	`data` tinytext,
	PRIMARY KEY (`deviceID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `device`
	(`title`	,`name`			,`type`	,`data`) VALUES
	('Stairs'	,'landing.stairs'	,'x10'	,'{"house":"B","unit":"1","type":"lamp"}'),
	('Metal Shelf'	,'downstairs.metalShelf','x10'	,'{"house":"B","unit":"2","type":"lamp"}'),
	('Nightstand'	,'guest.nightStand'	,'x10'	,'{"house":"B","unit":"5","type":"lamp"}'),
	('Lamp'		,'guest.lamp'		,'x10'	,'{"house":"B","unit":"6","type":"lamp"}'),
	('Nightstand'	,'posiBed.nightStand'	,'x10'	,'{"house":"B","unit":"9","type":"lamp"}'),
	('Lamp'		,'posiBed.lamp'		,'x10'	,'{"house":"B","unit":"10","type":"lamp"}'),
	('LED Net'	,'posiBed.ledNet'	,'x10'	,'{"house":"B","unit":"11","type":"lamp"}'),
	('Garage Lights','outside.front.door'	,'x10'	,'{"house":"B","unit":"13","type":"lamp"}'),
	('Side Door'	,'outside.north.door'	,'x10'	,'{"house":"B","unit":"14","type":"lamp"}'),
	('Back Door'	,'outside.rear.door'	,'x10'	,'{"house":"B","unit":"15","type":"lamp"}'),
	('Backyard Lights','outside.yard.lights','x10'	,'{"house":"B","unit":"16","type":"appliance"}'),
	('Lights'	,'garage.lights'	,'x10'	,'{"house":"G","unit:"1"","type":"appliance"}'),
	('Work Bench'	,'garage.bench'		,'x10'	,'{"house":"G","unit:"2"","type":"appliance"}');

DROP TABLE IF EXISTS `menuControl`;
CREATE TABLE `menuControl` (
	`ctlID` int(11) NOT NULL AUTO_INCREMENT,
	`menuGrpID` int(11) NOT NULL,
	`x` int(11) DEFAULT NULL,
	`dx` int(11) NOT NULL,
	`y` int(11) DEFAULT NULL,
	`dy` int(11) NOT NULL,
	`deviceName` tinytext NOT NULL,
	PRIMARY KEY (`ctlID`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

INSERT INTO `menuControl` 
(`menuGrpID`	, `x`	, `dx`	, `y`	, `dy`	, `deviceName`) VALUES
(1		, 0	, 100	, 0	, 100	, 'landing.stairs'),
(4		, 0	, 100	, 0	, 100	, 'landing.stairs'),
(4 		, 100	, 100	, 0	, 100	, 'downstairs.metalShelf'),
(3 		, 0	, 100	, 0	, 100	, 'guest.nightStand'),
(3 		, 100	, 100	, 0	, 100	, 'guest.lamp'),
(2 		, 0	, 100	, 0	, 100	, 'posiBed.nightStand'),
(2 		, 100	, 100	, 0	, 100	, 'posiBed.lamp'),
(2 		, 0	, 100	, 100	, 100	, 'posiBed.ledNet'),
(6 		, 0	, 100	, 0	, 100	, 'outside.front.door'),
(6 		, 100	, 100	, 0	, 100	, 'outside.north.door'),
(6		, 0	, 100	, 100	, 100	, 'outside.rear.door'),
(6 		, 100	, 100	, 100	, 100	, 'outside.yard.lights'),
(5 		, 0	, 100	, 0	, 100	, 'garage.lights'),
(5		, 100	, 100	, 0	, 100	, 'garage.bench');

DROP TABLE IF EXISTS `group`;
CREATE TABLE `group` (
  `grpID` int(11) NOT NULL AUTO_INCREMENT,
  `parentGrpID` int(11) NOT NULL,
  `name` tinytext,
  PRIMARY KEY (`grpID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

INSERT INTO `group` (`grpID`, `parentGrpID`, `name`) VALUES
(0, -1, 'Strange Attractor'),
(1, 0, 'Upstairs'),
(2, 1, 'Posi\'s Bedroom'),
(3, 1, 'Guest Bedroom'),
(4, 0, 'Downstairs'),
(5, 0, 'Garage'),
(6, 0, 'Outside');

DROP TABLE IF EXISTS `module`;
CREATE TABLE `module` (
  `modID` int(11) NOT NULL AUTO_INCREMENT,
  `name` tinytext,
  `path` tinytext,
  PRIMARY KEY (`modID`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

INSERT INTO `module` (`name`, `path`) VALUES
('x10', 'x10.cgi');

