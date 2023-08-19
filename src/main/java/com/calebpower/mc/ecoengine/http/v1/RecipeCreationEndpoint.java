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
package com.calebpower.mc.ecoengine.http.v1;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collector;

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Recipe;
import com.calebpower.mc.ecoengine.model.Cookbook;
import com.calebpower.mc.ecoengine.model.Recipe.Work;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the creation of cookbook recipes.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class RecipeCreationEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public RecipeCreationEndpoint() {
    super("/cookbooks/:cookbook/recipes", APIVersion.VERSION_1, HTTPMethod.POST);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONObject reqBody = new JSONObject(req.body());

      Cookbook cookbook = null;
      try {
        cookbook = Database.getInstance().getCookbook(
            UUID.fromString(
                req.params("cookbook")));
      } catch(IllegalArgumentException e) { }

      if(null == cookbook)
        throw new EndpointException(req, "Cookbook not found.", 404);

      UUID product = null;
      try {
        product = UUID.fromString(reqBody.getString("product"));
      } catch(IllegalArgumentException e) { }

      if(null == product
          || !cookbook.getPantry().contains(product))
        throw new EndpointException(req, "Unsupported product.", 404);

      int yield = 0;
      try {
        yield = reqBody.getInt("yield");
      } catch(JSONException e) { }

      if(0 >= yield)
        throw new EndpointException(req, "Yield out of bounds.", 400);

      Work work = null;
      try {
        work = Work.valueOf(reqBody.getString("work"));
      } catch(IllegalArgumentException | NullPointerException e) {
        throw new EndpointException(req, "Invalid work method.", 400, e);
      }

      float cost = -1f;
      try {
        cost = reqBody.getFloat("cost");
      } catch(JSONException e) { }

      if(0 > cost)
        throw new EndpointException(req, "Cost out of bounds.", 400);

      UUID id = null;
      do id = UUID.randomUUID(); while(null != Database.getInstance().getRecipe(id));

      Recipe recipe = new Recipe(
          id,
          cookbook.getID(),
          product,
          yield,
          work,
          cost);

      for(Object obj : reqBody.getJSONArray("ingredients")) {
        JSONObject ingredient = (JSONObject)obj;
        UUID iID = null;
        try {
          iID = UUID.fromString(ingredient.getString("id"));
        } catch(IllegalArgumentException e) { }
        
        if(null == iID
            || !cookbook.getPantry().contains(iID))
          throw new EndpointException(req, "Unsupported ingredient.", 404);

        int quantity = 0;
        try {
          quantity = ingredient.getInt("quantity");
        } catch(JSONException e) { }

        if(0 >= quantity)
          throw new EndpointException(req, "Invalid ingredient quantity.", 400);

        recipe.setIngredient(iID, quantity);
      }

      res.status(201);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "Created recipe.")
          .put("recipe", new JSONObject()
              .put("id", id.toString())
              .put("cookbook", cookbook.getID().toString())
              .put("product", product.toString())
              .put("yield", yield)
              .put("work", work.name())
              .put("cost", cost)
              .put("ingredients", (JSONArray)recipe.getIngredients().entrySet()
                  .stream()
                  .map(i -> new JSONObject()
                      .put("id", i.getKey().toString())
                      .put("quantity", i.getValue()))
                  .collect(
                      Collector.of(
                          JSONArray::new,
                          JSONArray::put,
                          JSONArray::put))));
      
    } catch(IllegalArgumentException | JSONException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
