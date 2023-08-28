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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Models some in-game item to be created, bought, and sold.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Commodity {

  private UUID id = null;
  private UUID abstraction = null;
  private String label = null;
  private Set<UUID> recipes = new HashSet<>();
  private Set<UUID> usages = new HashSet<>();
  private Set<UUID> implementations = new HashSet<>();

  /**
   * Instantiates a {@link Commodity} object.
   *
   * @param id the unique {@link UUID}
   * @param label the human-readable description
   */
  public Commodity(UUID id, UUID abstraction, String label) {
    this.id = id;
    this.abstraction = abstraction;
    this.label = label;
  }

  /**
   * Retrieves the unique identifier associated with the commodity.
   *
   * @return the commodity's unique {@link UUID}
   */
  public UUID getID() {
    return id;
  }

  /**
   * Retrieves the human-readable description associated with the commodity.
   *
   * @return the commodity's label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the human-readable description associated with the commodity.
   *
   * @param label the commodity's label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Retrieves the set of recipes associated with this commodity. That is, the
   * unique identifiers of the recipes that can be used to create this commodity.
   *
   * @return an unmodifiable {@link Set} of unique {@link UUID} objects
   */
  public Set<UUID> getRecipes() {
    return Collections.unmodifiableSet(recipes);
  }

  /**
   * Adds a recipe to the set of known recipes for this commodity. That is, adds
   * the unique identifier of a recipe that can be used to create this commodity.
   *
   * @param recipe the unique {@link UUID} of some recipe
   */
  public void addRecipe(UUID recipe) {
    this.recipes.add(recipe);
  }

  /**
   * Removes a recipe from the set of known recipes for this commodity.
   *
   * @param recipe the unique {@link UUID} of some recipe
   */
  public void removeRecipe(UUID recipe) {
    this.recipes.remove(recipe);
  }

  /**
   * Retrieves the set of usages associated with this commodity. That is, the
   * unique identifiers of the recipes for which this commodity is an ingredient.
   *
   * @return an unmodifiable {@link Set} of unique {@link UUID} objects
   */
  public Set<UUID> getUsages() {
    return Collections.unmodifiableSet(usages);
  }

  /**
   * Adds a usage to the set of known usages for this commidity. That is, adds
   * the unique identifier of a recipe for which this commodity is an ingredient.
   *
   * @param usage the unique {@link UUID} of some recipe
   */
  public void addUsage(UUID usage) {
    this.usages.add(usage);
  }

  /**
   * Removes a usage from the set of known usages for this commodity.
   *
   * @param usage the unique {@link UUID} of some recipe
   */
  public void removeUsage(UUID usage) {
    this.usages.remove(usage);
  }

  /**
   * Retrieves the unique identifier of the commodity that serves as the
   * abstraction for this one.
   *
   * @return a {@link UUID} serving as the unique ID of the abstraction
   */
  public UUID getAbstraction() {
    return abstraction;
  }

  /**
   * Sets the unique identifier of the commodity that serves as the abstraction
   * for this one.
   *
   * @param commodity the {@link UUID} serving as the unique ID of the
   *        abstraction, or {@code null} if there is no abstraction
   */
  public void setAbstraction(UUID commodity) {
    this.abstraction = commodity;
  }
  
  /**
   * Retrieves the set of implementations for which this commodity serves as the
   * abstraction.
   *
   * @return an unmodifiable {@link Set} of unique {@link UUID} objects
   */
  public Set<UUID> getImplementations() {
    return Collections.unmodifiableSet(implementations);
  }

  /**
   * Adds an implementation to the set of known implementations for which this
   * commodity is an abstraction.
   *
   * @param commodity the {@link UUID} associated with the commodity
   *        implementing this abstraction
   */
  public void addImplementation(UUID commodity) {
    this.implementations.add(commodity);
  }

  /**
   * Removes an implementation from the set of known implementations for which
   * this commodity serves as the abstraction.
   *
   * @param commodity the {@link UUID} of the commodity that no longer
   *        implements this abstraction
   */
  public void removeImplementation(UUID commodity) {
    this.implementations.remove(commodity);
  }
  
}
