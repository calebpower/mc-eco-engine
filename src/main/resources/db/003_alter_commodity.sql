ALTER TABLE ${database}.${prefix}commodity (
  ADD COLUMN IF NOT EXISTS abstraction BINARY(16)
    DEFAULT NULL
    AFTER label,
  ADD CONSTRAINT FOREIGN KEY IF NOT EXISTS (abstraction)
    REFERENCES ${prefix}commodity (id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
)Engine=InnoDB;
