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

  private UUID id = null;
  private UUID product = null;
  private UUID cookbook = null;
  private Map<UUID, Integer> ingredients = new HashMap<>();
  private Work work = null;
  private float cost = 0f;
  private int yield = 0;

  /**
   * Describes the manner in which the item is generated.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public static enum Work {

    /**
     * Indicates that the item is purchased. In this case, any ingredients
     * present indicate that they are required items for the purchase in
     * addition to the flat cost.
     */
    PURCHASE,

    /**
     * Indicates that the item is gathered or mined. During analysis, the cost
     * is multiplied by the sum of the ingredient costs.
     */
    OBTAIN,

    /**
     * Indicates that the item is crafted. During analysis, the cost is
     * is multiplied by the sum of the ingredient costs.
     */
    CRAFT,

    /**
     * Indicates that the item is smelted. During analysis, the cost is
     * multiplied by the sum of the ingredient costs.
     */
    SMELT,

    /**
     * Indicates that waiting is required. During analysis, the cost is
     * multiplied by the sum of the ingredient costs.
     */
    WAIT,

    /**
     * Indicates that the item is being sold. In this case, the product yield
     * and ingredients are basically reversed in the transaction--that is,
     * you are selling the product and getting the items / cost in payment.
     */
    SELL
  }

  /**
   * Instantiates a {@link Recipe} object.
   *
   * @param id the unique {@link UUID} associated with this recipe
   * @param cookbook the unique {@link UUID} associated with the cookbook
   *        containing this particular recipe
   * @param product the unique {@link UUID} associated with the product of this
   *        recipe
   * @param workMethod the {@link Work} method associated with the mutation
   *        procedure
   * @param workAmount the amount of work that needs to be put into the
   *        transition
   */
  public Recipe(UUID id, UUID cookbook, UUID product, int yield, Work work, float cost) {
    this.id = id;
    this.cookbook = cookbook;
    this.product = product;
    this.yield = yield;
    this.work = work;
    this.cost = cost;
  }

  /**
   * Instantiates a {@link Recipe} object.
   *
   * @param id the unique {@link UUID} associated with this recipe
   * @param recipe the unique {@link UUID} of the recipe to be copied
   */
  public Recipe(UUID id, Recipe recipe) {
    this.id = id;
    this.product = recipe.product;
    this.yield = recipe.yield;
    this.work = recipe.work;
    this.cost = recipe.cost;
    this.ingredients.putAll(recipe.ingredients);
  }

  /**
   * Retrieves the recipe's unique identifier.
   *
   * @return the recipe's unique {@link UUID}
   */
  public UUID getID() {
    return id;
  }

  /**
   * Retrieves the parent cookbook's unique identifier.
   *
   * @return the cookbook's unique {@link UUID}
   */
  public UUID getCookbook() {
    return cookbook;
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
   * Replaces this recipe's ingredients with those of the provided map.
   *
   * @param ingredients the new ingredients for this recipe
   */
  public void replaceIngredients(Map<UUID, Integer> ingredients) {
    this.ingredients.clear();
    this.ingredients.putAll(ingredients);
  }

  /**
   * Gets the manner or method by which the product is produced.
   *
   * @return the {@link Work} method
   */
  public Work getWork() {
    return work;
  }

  /**
   * Retrieves the amount of work required by this recipe.
   *
   * @return some quantity or amount of work
   */
  public float getCost() {
    return cost;
  }

  /**
   * Sets both the manner or method by which the product is produced, as well as
   * the amount required.
   *
   * @param method the {@link Work} method required
   * @param cost the quantity or amount of work required
   */
  public void setWork(Work method, float cost) {
    this.work = method;
    this.cost = cost;
  }

  /**
   * Retrieves the amount of product generated by this recipe.
   *
   * @return some quantity or amount of product
   */
  public int getYield() {
    return yield;
  }

  /**
   * Sets the amount of product generated by this recipe.
   *
   * @param yield the quantity or amount of product generated
   */
  public void setYield(int yield) {
    this.yield = yield;
  }
  
}
