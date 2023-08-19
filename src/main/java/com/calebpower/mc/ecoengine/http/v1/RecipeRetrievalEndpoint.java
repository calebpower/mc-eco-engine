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

import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the retrieval of an individual recipe.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class RecipeRetrievalEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public RecipeRetrievalEndpoint() {
    super("/cookbooks/:cookbook/recipes/:recipe", APIVersion.VERSION_1, HTTPMethod.GET);
  }
  
  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    UUID cookbook = null;
    Recipe recipe = null;
    try {
      cookbook = UUID.fromString(req.params("cookbook"));
      recipe = Database.getInstance().getRecipe(
          UUID.fromString(
              req.params("recipe")));
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    } catch(IllegalArgumentException e) { }

    if(null == cookbook
        || null == recipe
        || 0 != recipe.getCookbook().compareTo(cookbook))
      throw new EndpointException(req, "Recipe not found.", 404);

    res.status(200);
    return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved recipe.")
        .put("recipe", new JSONObject()
            .put("id", recipe.getID().toString())
            .put("cookbook", cookbook.toString())
            .put("product", recipe.getProduct().toString())
            .put("yield", recipe.getYield())
            .put("work", recipe.getWork().name())
            .put("cost", recipe.getCost())
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
  }
  
}
