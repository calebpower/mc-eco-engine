/*
 * Copyright (c) 2023 Caleb L. Power et. al.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  private Set<UUID> supportedCommodities = new HashSet<>();
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
   * Instantiates a child {@link Workbook} object, potentially as a copy of some
   * parent workbook. One or both of the  arguments {@code parent} and
   * {@code description} must be specified.
   *
   * @param id the new workbook's unique identifier
   * @param parent the parent from which this workbook shall be created, or
   *        {@code null} if this workbook has no parent
   * @param description the new workbook's description, or {@code null} if
   *        the parent's description should be used
   */
  public Workbook(UUID id, Workbook parent, String description) {
    if(null == parent && null == description)
      throw new IllegalArgumentException("parent and/or description must be specified");
    this.id = id;
    if(null != parent) {
      this.parent = parent.parent;
      this.children.addAll(parent.children);
      this.supportedCommodities.addAll(parent.supportedCommodities);
    }
    this.description = null == description ? parent.description : description;
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
   * Retrieves the set of unique identifiers associated with commodities
   * supported by this workbook.
   *
   * @return an unmodifiable {@link Set} comprised of unique {@link UUID} objects
   */
  public Set<UUID> getSupportedCommodities() {
    return Collections.unmodifiableSet(supportedCommodities);
  }

  /**
   * Adds a commodity to the list of commodities supported by this workbook.
   *
   * @param commodity the unique {@link UUID} of the commodity to support
   */
  public void addSupportedCommodity(UUID commodity) {
    supportedCommodities.add(commodity);
  }

  /**
   * Removes a commodity from the list of commodities supported by this workbook.
   *
   * @param the unique {@link UUID} of the commodity for which to remove support
   */
  public void removeSupportedCommodity(UUID commodity) {
    supportedCommodities.remove(commodity);
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
