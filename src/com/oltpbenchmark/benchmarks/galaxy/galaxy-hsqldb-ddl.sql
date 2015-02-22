-- Drop tables before creating them
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS solarsystems;
DROP TABLE IF EXISTS ships;


-- classes table holds all the ship classes and their reachability
CREATE TABLE CLASSES
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
CREATE TABLE SOLARSYSTEMS
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
CREATE TABLE SHIPS
(
  sid integer   NOT NULL,
  x integer     NOT NULL,
  y integer     NOT NULL,
  class integer NOT NULL REFERENCES CLASSES (cid),
  ssid integer  NOT NULL REFERENCES solarsystems (ssid),
  PRIMARY KEY
  (
    sid
  )
);
CREATE INDEX idx_ships_x ON ships (x);
CREATE INDEX idx_ships_y ON ships (y);
