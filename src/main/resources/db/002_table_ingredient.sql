CREATE TABLE IF NOT EXISTS ${database}.${prefix}ingredient (
  recipe BINARY(16) NOT NULL,
  commodity BINARY(16) NOT NULL,
  amount TINYINT UNSIGNED NOT NULL,
  last_update TIMESTAMP
    DEFAULT CURRENT_TIMESTAMP
    NOT NULL,
  PRIMARY KEY (recipe, commodity),
  FOREIGN KEY (recipe) REFERENCES ${prefix}recipe
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (commodity) REFERENCES ${prefix}commodity
    ON UPDATE CASCADE
    ON DELETE CASCADE
)Engine=InnoDB;
