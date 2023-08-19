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
import java.util.Set;
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
 * Facilitates the listing of recipes in a cookbook.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class RecipeListingEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public RecipeListingEndpoint() {
    super("/cookbooks/:cookbook/recipes", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      Set<Recipe> recipes = null;
      UUID cookbook = null;

      try {
        recipes = Database.getInstance().getCookbookRecipes(
            cookbook = UUID.fromString(
                req.params("cookbook")));
      } catch(IllegalArgumentException e) { }

      if(null == recipes)
        throw new EndpointException(req, "Cookbook not found.", 404);

      res.status(200);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved recipes.")
        .put("cookbook", cookbook.toString())
        .put("recipes", recipes
             .stream()
             .map(r -> r.getID().toString())
             .collect(
                 Collector.of(
                     JSONArray::new,
                     JSONArray::put,
                     JSONArray::put)));
      
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
