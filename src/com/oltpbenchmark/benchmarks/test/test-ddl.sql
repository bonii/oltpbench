-- Drop all views / tables

--IF OBJECT_ID('V_VOTES_BY_PHONE_NUMBER') IS NOT NULL DROP view V_VOTES_BY_PHONE_NUMBER;
--IF OBJECT_ID('V_VOTES_BY_CONTESTANT_NUMBER_STATE') IS NOT NULL DROP view V_VOTES_BY_CONTESTANT_NUMBER_STATE;
--IF OBJECT_ID('AREA_CODE_STATE') IS NOT NULL DROP table AREA_CODE_STATE;
--IF OBJECT_ID('VOTES') IS NOT NULL DROP table VOTES;
--IF OBJECT_ID('CONTESTANTS') IS NOT NULL DROP table CONTESTANTS;


-- classes table holds all the ship classes and their reachability
CREATE TABLE classes
(
  cid integer           NOT NULL,
  class varchar(40)     NOT NULL,
  reachability integer  NOT NULL,
  PRIMARY KEY
  (
    cid
  )
);

-- solarsystems table holds all solarsystems and their size
CREATE TABLE solarsystems
(
  ssid integer NOT NULL,
  xmax integer NOT NULL,
  ymax integer NOT NULL,
  PRIMARY KEY
  (
    ssid
  )
);

-- ships table holds every ship, and their position
CREATE TABLE ships
(
  sid integer   NOT NULL,
  x integer     NOT NULL,
  y integer     NOT NULL,
  class integer NOT NULL REFERENCES classes (class),
  ssid integer  NOT NULL REFERENCES solarsystems (ssid),
  PRIMARY KEY
  (
    sid
  )
);
