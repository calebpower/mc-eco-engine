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
  private String label = null;
  private Set<UUID> recipes = new HashSet<>();
  private Set<UUID> usages = new HashSet<>();

  /**
   * Instantiates a {@link Commodity} object.
   *
   * @param id the unique {@link UUID}
   * @param label the human-readable description
   */
  public Commodity(UUID id, String label) {
    this.id = id;
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
   * @param usgae the unique {@link UUID} of some recipe
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
  
}
