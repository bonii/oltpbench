-- Drop tables before creating them
DROP TABLE IF EXISTS FITTINGS;
DROP TABLE IF EXISTS FITTING;
DROP TABLE IF EXISTS SHIPS;
DROP TABLE IF EXISTS CLASSES;
DROP TABLE IF EXISTS SOLAR_SYSTEMS;

-- classes table holds all the ship classes and class related information
CREATE TABLE CLASSES
(
  class_id            integer     NOT NULL,
  class_name          varchar(40) NOT NULL,
  reachability        bigint      NOT NULL,
  base_health_points  integer     NOT NULL,
  max_fittings_count  integer     NOT NULL,
  PRIMARY KEY
  (
    class_id
  )
);

-- solar_systems table holds all solar systems, their size and security level
CREATE TABLE SOLAR_SYSTEMS
(
  solar_system_id integer NOT NULL,
  max_position_x  bigint  NOT NULL,
  max_position_y  bigint  NOT NULL,
  max_position_z  bigint  NOT NULL,
  security_level  integer NOT NULL,
  PRIMARY KEY
  (
    solar_system_id
  )
);

-- ships table holds every ship, their position and current health points
CREATE TABLE SHIPS
(
  ship_id         integer NOT NULL,
  position_x      bigint  NOT NULL,
  position_y      bigint  NOT NULL,
  position_z	  bigint  NOT NULL,
  class_id        integer NOT NULL REFERENCES CLASSES (class_id),
  solar_system_id integer NOT NULL REFERENCES SOLAR_SYSTEMS (solar_system_id),
  health_points   integer NOT NULL,
  PRIMARY KEY
  (
    ship_id
  )
);
CREATE INDEX idx_ships_x ON SHIPS (position_x);
CREATE INDEX idx_ships_y ON SHIPS (position_y);
CREATE INDEX idx_ships_z ON SHIPS (position_z);

-- fitting table holds information about a fitting
CREATE TABLE FITTING
(
  fitting_id    integer NOT NULL,
  fitting_type  integer NOT NULL,
  fitting_value integer NOT NULL,
  PRIMARY KEY
  (
    fitting_id
  )
);

-- fittings table hold the links between ships and fittings
CREATE TABLE FITTINGS
(
  fittings_id integer NOT NULL,
  ship_id     integer NOT NULL REFERENCES SHIPS (ship_id),
  fitting_id  integer NOT NULL REFERENCES FITTING (fitting_id),
  PRIMARY KEY
  (
    fittings_id
  )
);
