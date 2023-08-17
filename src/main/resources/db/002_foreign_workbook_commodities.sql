CREATE TABLE IF NOT EXISTS ${database}.${prefix}workbook_commodities (
  workbook BINARY(16) NOT NULL,
  commodity BINARY(16) NOT NULL,
  last_update TIMESTAMP
    DEFAULT CURRENT_TIMESTAMP
    NOT NULL,
  PRIMARY KEY (workbook, commodity),
  FOREIGN KEY (workbook) REFERENCES ${prefix}workbook
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  FOREIGN KEY (commodity) REFERENCES ${prefix}commodity
    ON UPDATE CASCADE
    ON DELETE CASCADE
)Engine=InnoDB;
