package com.calebpower.mc.ecoengine.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Models a recipe--that is, some manner by which a set of items is, through some
 * amount of work, transmuted into a quantity of some other item.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Recipe {

  private String label = null;
  private UUID id = null;
  private UUID product = null;
  private Map<UUID, Integer> ingredients = new HashMap<>();
  private Work workMethod = null;
  private double workAmount = 0d;

  /**
   * Describes the manner in which the item is generated.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public static enum Work {

    /**
     * Indicates that the item is purchased.
     */
    PURCHASE,

    /**
     * Indicates that the item is gathered or mined.
     */
    OBTAIN,

    /**
     * Indicates that the item is crafted.
     */
    CRAFT,

    /**
     * Indicates that the item is smelted.
     */
    SMELT
  }

  /**
   * Instantiates a {@link Recipe} object.
   *
   * @param id the unique {@link UUID} associated with this recipe
   * @param label the human-readable label associated with the recipe
   * @param product the unique {@link UUID} associated with the product of this
   *        recipe
   * @param workMethod the {@link Work} method associated with the mutation
   *        procedure
   * @param workAmount the amount of work that needs to be put into the
   *        transition
   */
  public Recipe(UUID id, String label, UUID product, Work workMethod, double workAmount) {
    this.id = id;
    this.label = label;
    this.product = product;
    this.workMethod = workMethod;
    this.workAmount = workAmount;
  }

  /**
   * Instantiates a {@link Recipe} object.
   *
   * @param id the unique {@link UUID} associated with this recipe
   * @param recipe the unique {@link UUID} of the recipe to be copied
   */
  public Recipe(UUID id, Recipe recipe) {
    this.id = id;
    this.label = recipe.label;
    this.product = recipe.product;
    this.workMethod = recipe.workMethod;
    this.workAmount = recipe.workAmount;
    this.ingredients.putAll(recipe.ingredients);
  }

  /**
   * Retrieves the recipe's public identifier.
   *
   * @return the recipe's unique {@link UUID}
   */
  public UUID getID() {
    return id;
  }

  /**
   * Retrieves the label associated with the recipe.
   *
   * @return the recipe's label
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the label associated with the recipe.
   *
   * @param the recipe's label
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Retrieves the unique identifier of the product that this recipe is supposed
   * to generate.
   *
   * @return the product's unique {@link UUID}
   */
  public UUID getProduct() {
    return product;
  }

  /**
   * Sets the product that this recipe is supposed to generate.
   *
   * @return the product's unique {@link UUID}
   */
  public void setProduct(UUID product) {
    this.product = product;
  }

  /**
   * Retrieves the set of ingredients required to produce the produce in
   * accordance with this recipe.
   *
   * @return a {@link Map} of unique {@link UUID} objects and their respective
   *         integers (denoting quantity)
   */
  public Map<UUID, Integer> getIngredients() {
    return Collections.unmodifiableMap(ingredients);
  }

  /**
   * Adds or updates an ingredient required for production.
   *
   * @param the unique {@link UUID} associated with the ingredient
   * @param quantity the amount of that ingredient necessary
   */
  public void setIngredient(UUID ingredient, int quantity) {
    ingredients.put(ingredient, quantity);
  }

  /**
   * Removes an ingredient from the set of ingredients for this recipe.
   *
   * @param the unique {@link UUID} of the ingredient in question
   */
  public void removeIngredient(UUID ingredient) {
    ingredients.remove(ingredient);
  }

  /**
   * Gets the manner or method by which the product is produced.
   *
   * @return the {@link Work} method
   */
  public Work getWorkMethod() {
    return workMethod;
  }

  /**
   * Retrieves the amount of work required by this recipe.
   *
   * @return some quantity or amount of work
   */
  public double getWorkAmount() {
    return workAmount;
  }

  /**
   * Sets both the manner or method by which the product is produced, as well as
   * the amount required.
   *
   * @param method the {@link Work} method required
   * @param amount the quantity or amount of work required
   */
  public void setWork(Work method, double amount) {
    this.workMethod = method;
    this.workAmount = amount;
  }
  
}
