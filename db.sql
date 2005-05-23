--
-- Table structure for table `nicks`
--

CREATE TABLE nicks (
  id int(11) NOT NULL auto_increment,
  nick varchar(50) default NULL,
  pass varchar(32) default NULL,
  body int(11) default NULL,
  PRIMARY KEY  (id),
  KEY index_skore (body),
  KEY index_login (nick,pass),
  KEY index_nick_id (id)
) TYPE=MyISAM;

--
-- Table structure for table `online`
--

CREATE TABLE online (
  id int(11) NOT NULL auto_increment,
  nick varchar(50) default NULL,
  PRIMARY KEY  (id)
) TYPE=MyISAM;

--
-- Table structure for table `otazky`
--

CREATE TABLE otazky (
  id int(11) NOT NULL auto_increment,
  otazka varchar(100) default NULL,
  odpoved varchar(100) default NULL,
  owner int(11) default NULL,
  tema int(11) default NULL,
  schvaleni int(11) default NULL,
  last datetime default NULL,
  change_tema int(11) default NULL,
  change_otazka varchar(100) default NULL,
  change_odpoved varchar(100) default NULL,
  change_comment varchar(100) default NULL,
  PRIMARY KEY  (id)
) TYPE=MyISAM;

--
-- Table structure for table `perm`
--

CREATE TABLE perm (
  id int(11) NOT NULL auto_increment,
  nick int(11) default NULL,
  perm char(1) default NULL,
  PRIMARY KEY  (id),
  KEY index_perm (perm),
  KEY index_perm_nick (nick)
) TYPE=MyISAM;


--
-- Table structure for table `temata`
--

CREATE TABLE temata (
  id int(11) NOT NULL auto_increment,
  nazev varchar(100) default NULL,
  hidden tinyint(4) default NULL,
  PRIMARY KEY  (id),
  KEY index_temata (nazev)
) TYPE=MyISAM;

