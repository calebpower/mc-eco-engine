CREATE TABLE IF NOT EXISTS ${database}.${prefix}recipe (
  id BINARY(16) NOT NULL,
  cookbook BINARY(16) NOT NULL,
  product BINARY(16) NOT NULL,
  product_yield TINYINT UNSIGNED NOT NULL,
  work_method TINYINT UNSIGNED NOT NULL,
  work_cost FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  FOREIGN KEY (product) REFERENCES ${prefix}recipe (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (cookbook) REFERENCES ${prefix}cookbook (id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
)Engine=InnoDB;
