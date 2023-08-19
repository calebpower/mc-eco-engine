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
  private UUID workbook = null;
  private Map<UUID, Integer> ingredients = new HashMap<>();
  private Work workMethod = null;
  private float workAmount = 0f;

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
   * @param workbook the unique {@link UUID} associated with the workbook
   *        containing this particular recipe
   * @param product the unique {@link UUID} associated with the product of this
   *        recipe
   * @param workMethod the {@link Work} method associated with the mutation
   *        procedure
   * @param workAmount the amount of work that needs to be put into the
   *        transition
   */
  public Recipe(UUID id, UUID workbook, UUID product, Work workMethod, float workAmount) {
    this.id = id;
    this.workbook = workbook;
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
    this.product = recipe.product;
    this.workMethod = recipe.workMethod;
    this.workAmount = recipe.workAmount;
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
   * Retrieves the parent workbook's unique identifier.
   *
   * @return the workbook's unique {@link UUID}
   */
  public UUID getWorkbook() {
    return workbook;
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
  public float getWorkAmount() {
    return workAmount;
  }

  /**
   * Sets both the manner or method by which the product is produced, as well as
   * the amount required.
   *
   * @param method the {@link Work} method required
   * @param amount the quantity or amount of work required
   */
  public void setWork(Work method, float amount) {
    this.workMethod = method;
    this.workAmount = amount;
  }
  
}
