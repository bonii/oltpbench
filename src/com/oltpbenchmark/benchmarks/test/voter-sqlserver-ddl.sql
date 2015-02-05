-- Drop all views / tables

--IF OBJECT_ID('V_VOTES_BY_PHONE_NUMBER') IS NOT NULL DROP view V_VOTES_BY_PHONE_NUMBER;
--IF OBJECT_ID('V_VOTES_BY_CONTESTANT_NUMBER_STATE') IS NOT NULL DROP view V_VOTES_BY_CONTESTANT_NUMBER_STATE;
--IF OBJECT_ID('AREA_CODE_STATE') IS NOT NULL DROP table AREA_CODE_STATE;
--IF OBJECT_ID('VOTES') IS NOT NULL DROP table VOTES;
--IF OBJECT_ID('CONTESTANTS') IS NOT NULL DROP table CONTESTANTS;


-- Map of Area Codes and States for geolocation classification of incoming calls
CREATE TABLE classes
(
  class integer NOT NULL,
  reachability integer NOT NULL,
  PRIMARY KEY
  (
    class
  )
);

-- votes table holds every valid vote.
--   voters are not allowed to submit more than <x> votes, x is passed to client application
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
CREATE INDEX idx_votes_phone_number ON votes (phone_number);

-- ships table holds every ship, and their position
CREATE TABLE ships
(
  sid integer NOT NULL,
  x integer NOT NULL,
  y integer NOT NULL,
  class integer NOT NULL REFERENCES classes (class),
  ssid integer NOT NULL REFERENCES solarsystems (ssid),
  PRIMARY KEY
  (
    sid
  )
);
