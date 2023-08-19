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
 * Models a cookbook--that is, a set of commodities and their respective recipes.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Cookbook {

  private UUID id = null;
  private UUID parent = null;
  private Set<UUID> children = new HashSet<>();
  private Set<UUID> supportedCommodities = new HashSet<>();
  private String description = null;
  private Timestamp timeCreated = null;
  private Timestamp timeModified = null;

  /**
   * Instantiates the {@link Cookkbook} object.
   *
   * @param id the cookbook's unique identifier
   * @param parent the unique identifier of the cookbook's parent
   * @param description the human-readable description of the cookbook
   * @param timeCreated the time at which this cookbook was first created
   * @param timeModified the time at which this cookbook was last modified
   */
  public Cookbook(UUID id, UUID parent, String description, Timestamp timeCreated, Timestamp timeModified) {
    this.id = id;
    this.parent = parent;
    this.description = description;
    this.timeCreated = timeCreated;
    this.timeModified = timeModified;
  }

  /**
   * Instantiates a child {@link Cookbook} object, potentially as a copy of some
   * parent cookbook. One or both of the  arguments {@code parent} and
   * {@code description} must be specified.
   *
   * @param id the new cookbook's unique identifier
   * @param parent the parent from which this cookbook shall be created, or
   *        {@code null} if this cookbook has no parent
   * @param description the new cookbook's description, or {@code null} if
   *        the parent's description should be used
   */
  public Cookbook(UUID id, Cookbook parent, String description) {
    if(null == parent && null == description)
      throw new IllegalArgumentException("parent and/or description must be specified");
    this.id = id;
    if(null != parent) {
      this.parent = parent.id;
      this.children.addAll(parent.children);
      this.supportedCommodities.addAll(parent.supportedCommodities);
    }
    this.description = null == description ? parent.description : description;
  }

  /**
   * Retrieves the unique identifier associated with this cookbook.
   *
   * @return the cookbook's unique {@link UUID}
   */
  public UUID getID() {
    return id;
  }

  /**
   * Retrieves the unique identifier associated with the cookbook's parent.
   *
   * @return the parent's unique {@link UUID}
   */
  public UUID getParent() {
    return parent;
  }

  /**
   * Retrieves the set of unique identifiers associated with the cookbook's
   * children.
   *
   * @return an unmodifiable {@link Set} comprised of unique {@link UUID} objects
   */
  public Set<UUID> getChildren() {
    return Collections.unmodifiableSet(children);
  }

  /**
   * Adds a known child to the set of this cookbook's children.
   *
   * @param child the unique {@link UUID} of the child in question
   */
  public void addChild(UUID child) {
    children.add(child);
  }

  /**
   * Removes a child from the set of this cookbook's children.
   *
   * @param child the unique {@link UUID} of the child in question
   */
  public void removeChild(UUID child) {
    children.remove(child);
  }

  /**
   * Retrieves the set of unique identifiers associated with commodities
   * supported by this cookbook.
   *
   * @return an unmodifiable {@link Set} comprised of unique {@link UUID} objects
   */
  public Set<UUID> getSupportedCommodities() {
    return Collections.unmodifiableSet(supportedCommodities);
  }

  /**
   * Adds a commodity to the list of commodities supported by this cookbook.
   *
   * @param commodity the unique {@link UUID} of the commodity to support
   */
  public void addSupportedCommodity(UUID commodity) {
    supportedCommodities.add(commodity);
  }

  /**
   * Removes a commodity from the list of commodities supported by this cookbook.
   *
   * @param the unique {@link UUID} of the commodity for which to remove support
   */
  public void removeSupportedCommodity(UUID commodity) {
    supportedCommodities.remove(commodity);
  }
  
  /**
   * Retrieves this cookbook's description.
   *
   * @return the cookbook's human-readable description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the cookbook's description.
   *
   * @param the cookbook's new description
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * The time at which this cookbook was first saved to the database.
   *
   * @return a {@link Timestamp} associated with the cookbook's creation time
   */
  public Timestamp getTimeCreated() {
    return timeCreated;
  }

  /**
   * The time at which this cookbook was last modified.
   *
   * @return a {@link Timestamp} associated with the cookbook's last modification
   */
  public Timestamp getTimeModified() {
    return timeModified;
  }
  
}
