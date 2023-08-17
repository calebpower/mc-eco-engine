package com.calebpower.mc.ecoengine.model;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Models a workbook--that is, a set of commodities and their respective recipes.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Workbook {

  private UUID id = null;
  private UUID parent = null;
  private Set<UUID> children = new HashSet<>();
  private String description = null;
  private Timestamp timeCreated = null;
  private Timestamp timeModified = null;

  /**
   * Instantiates the {@link Workbook} object.
   *
   * @param id the workbook's unique identifier
   * @param parent the unique identifier of the workbook's parent
   * @param description the human-readable description of the workbook
   * @param timeCreated the time at which this workbook was first created
   * @param timeModified the time at which this workbook was last modified
   */
  public Workbook(UUID id, UUID parent, String description, Timestamp timeCreated, Timestamp timeModified) {
    this.id = id;
    this.parent = parent;
    this.description = description;
    this.timeCreated = timeCreated;
    this.timeModified = timeModified;
  }

  /**
   * Instantiates a child {@link Workbook} object from some parent workbook.
   *
   * @param id the new workbook's unique identifier
   * @param parent the parent from which this workbook shall be created
   */
  public Workbook(UUID id, Workbook parent) {
    this.id = id;
    this.parent = parent.parent;
    this.description = parent.description;
    this.children.addAll(parent.children);
  }

  /**
   * Retrieves the unique identifier associated with this workbook.
   *
   * @return the workbook's unique {@link UUID}
   */
  public UUID getID() {
    return id;
  }

  /**
   * Retrieves the unique identifier associated with the workbook's parent.
   *
   * @return the parent's unique {@link UUID}
   */
  public UUID getParent() {
    return parent;
  }

  /**
   * Retrieves the set of unique identifiers associated with the workbook's
   * children.
   *
   * @return an unmodifiable {@link Set} comprised of unique {@link UUID} objects
   */
  public Set<UUID> getChildren() {
    return Collections.unmodifiableSet(children);
  }

  /**
   * Adds a known child to the set of this workbook's children.
   *
   * @param child the unique {@link UUID} of the child in question
   */
  public void addChild(UUID child) {
    children.add(child);
  }

  /**
   * Removes a child from the set of this workbook's children.
   *
   * @param child the unique {@link UUID} of the child in question
   */
  public void removeChild(UUID child) {
    children.remove(child);
  }

  /**
   * Retrieves this workbook's description.
   *
   * @return the workbook's human-readable description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the workbook's description.
   *
   * @param the workbook's new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The time at which this workbook was first saved to the database.
   *
   * @return a {@link Timestamp} associated with the workbook's creation time
   */
  public Timestamp getTimeCreated() {
    return timeCreated;
  }

  /**
   * The time at which this workbook was last modified.
   *
   * @return a {@link Timestamp} associated with the workbook's last modification
   */
  public Timestamp getTimeModified() {
    return timeModified;
  }
  
}
