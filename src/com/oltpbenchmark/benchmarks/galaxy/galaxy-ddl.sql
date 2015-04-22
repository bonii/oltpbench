-- Drop tables before creating them
DROP TABLE IF EXISTS fittings;
DROP TABLE IF EXISTS fitting;
DROP TABLE IF EXISTS ships;
DROP TABLE IF EXISTS classes;
DROP TABLE IF EXISTS solar_systems;

-- classes table holds all the ship classes and class related information
CREATE TABLE classes
(
  class_id            integer     NOT NULL,
  class_name          varchar(40) NOT NULL,
  reachability        integer     NOT NULL,
  base_health_points  integer     NOT NULL,
  max_fittings_count  integer     NOT NULL,
  PRIMARY KEY
  (
    class_id
  )
);

-- solar_systems table holds all solar systems, their size and security level
CREATE TABLE solar_systems
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
CREATE TABLE ships
(
  ship_id         integer NOT NULL,
  position_x      bigint  NOT NULL,
  position_y      bigint  NOT NULL,
  position_z	  bigint  NOT NULL,
  class_id        integer NOT NULL REFERENCES classes (class_id),
  solar_system_id integer NOT NULL REFERENCES solar_systems (solar_system_id),
  health_points   integer NOT NULL,
  PRIMARY KEY
  (
    ship_id
  )
);
CREATE INDEX idx_ships_x ON ships (position_x);
CREATE INDEX idx_ships_y ON ships (position_y);
CREATE INDEX idx_ships_z ON ships (position_z);

-- fitting table holds information about a fitting
CREATE TABLE fitting
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
CREATE TABLE fittings
(
  fittings_id integer NOT NULL,
  ship_id     integer NOT NULL REFERENCES ships (ship_id),
  fitting_id  integer NOT NULL REFERENCES fitting (fitting_id),
  PRIMARY KEY
  (
    fittings_id
  )
);
