CREATE TABLE IF NOT EXISTS ${database}.${prefix}pantry (
  cookbook BINARY(16) NOT NULL,
  commodity BINARY(16) NOT NULL,
  last_update TIMESTAMP
    DEFAULT CURRENT_TIMESTAMP
    NOT NULL,
  PRIMARY KEY (cookbook, commodity),
  FOREIGN KEY (cookbook) REFERENCES ${prefix}cookbook (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (commodity) REFERENCES ${prefix}commodity (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
)Engine=InnoDB;
