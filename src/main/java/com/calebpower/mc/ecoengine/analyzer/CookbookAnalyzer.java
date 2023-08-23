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
package com.calebpower.mc.ecoengine.analyzer;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.model.Commodity;
import com.calebpower.mc.ecoengine.model.Cookbook;
import com.calebpower.mc.ecoengine.model.Recipe;
import com.calebpower.mc.ecoengine.model.Recipe.Work;

/**
 * Conducts an analysis on one or more cookbooks.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookAnalyzer {

  private Map<UUID, Commodity> commodities = null;
  private Map<UUID, Set<Recipe>> recipes = null;

  /**
   * Instantiates the analyzer.
   *
   * @param cookbook the {@link Cookbook} to analyze
   * @throws SQLException if a database malfunction occurs
   */
  public CookbookAnalyzer(Cookbook cookbook) throws SQLException {
    commodities = Database.getInstance()
        .getCommodities()
        .stream()
        .filter(c -> cookbook.getPantry().contains(c.getID()))
        .collect(
            Collectors.toMap(
                c -> c.getID(),
                c -> c));
    
    recipes = Database.getInstance()
        .getCookbookRecipes(cookbook.getID())
        .stream()
        .collect(
            Collectors.groupingBy(
                r -> r.getProduct(),
                Collectors.mapping(
                    r -> r,
                    Collectors.toSet())));
  }

  /**
   * Performs an analysis of the {@link Cookbook} associated with this analyzer.
   *
   * @return the {@link Analysis} results
   */
  public Analysis analyze() {    
    Analysis analysis = new Analysis();

    for(var commodity : commodities.keySet()) {
      try {
        analysis.addFungibleCommodity(
            commodity,
            getValue(commodity, new HashSet<>()));
      } catch(NonfungibleCommodityException e) {
        analysis.addNonfungibleCommodity(commodity);
      }
    }
    
    return analysis;
  }

  private float getValue(UUID commodity, Set<UUID> chain) throws NonfungibleCommodityException {
    float minCost = 0f;
    Set<UUID> appended = new HashSet<>(chain);
    int validRecipes = 0;

    if(recipes.containsKey(commodity)) {
      recipes: for(var recipe : recipes.get(commodity)) {
        float cost = 0f;
        for(var ingredient : recipe.getIngredients().entrySet()) {
          if(chain.contains(ingredient.getKey())) continue recipes; // prevent infinite loop
          
          try {
            cost += getValue(ingredient.getKey(), appended) * ingredient.getValue();
          } catch(NonfungibleCommodityException e) {
            continue recipes;
          }
        }
        
        cost = Work.PURCHASE == recipe.getWork() ? cost + recipe.getCost() : cost * recipe.getCost();
        
        if(minCost >= cost)
          minCost = cost;
        
        validRecipes++;
      }
    }

    if(0 == validRecipes) throw new NonfungibleCommodityException(commodities.get(commodity));

    return minCost;
  }

  /**
   * Computes the difference between this {@link Cookbook} and the one provided.
   * It's assumed that the one provided is newer than this one.
   *
   * @param cookbook the newer {@link Cookbook} to compare
   * @return the {@link Diff} object representing the comparison
   * @throws SQLException if a database malfunction occurs
   */
  public Diff diff(Cookbook cookbook) throws SQLException {
    Objects.requireNonNull(cookbook);
    Analysis a1 = analyze();
    Analysis a2 = new CookbookAnalyzer(cookbook).analyze();

    Diff diff = new Diff(a1, a2);
    for(var commodity : diff.getCommodityUnion()) {
      if(commodities.containsKey(commodity) && cookbook.getPantry().contains(commodity)) {
        diff.addModifiedCommodity(
            commodity,
            (a2.isFungible(commodity) ? a2.getFungibleCommodities().get(commodity) : 0)
                - (a1.isFungible(commodity) ? a1.getFungibleCommodities().get(commodity) : 0));
      } else if(commodities.containsKey(commodity) && !cookbook.getPantry().contains(commodity)) {
        diff.addDisappearingCommodity(commodity);
      } else if(!commodities.containsKey(commodity) && cookbook.getPantry().contains(commodity)) {
        diff.addNewCommodity(commodity);
      } else {
        throw new RuntimeException("failed to get diff, as we found a ghost commodity");
      }
    }

    return diff;
  }
  
}
