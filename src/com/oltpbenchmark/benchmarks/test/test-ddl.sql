-- Drop tables before creating them
--DROP TABLE IF EXISTS classes, solarsystems, ships;


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
  x_max integer NOT NULL,
  y_max integer NOT NULL,
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
  class integer NOT NULL REFERENCES classes (cid),
  ssid integer  NOT NULL REFERENCES solarsystems (ssid),
  PRIMARY KEY
  (
    sid
  )
);
