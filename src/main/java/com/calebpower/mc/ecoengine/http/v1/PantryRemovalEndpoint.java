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

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Cookbook;

import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the removal of commodities from a cookbook's pantry.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class PantryRemovalEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public PantryRemovalEndpoint() {
    super("/cookbooks/:cookbook/pantry/:commodity", APIVersion.VERSION_1, HTTPMethod.DELETE);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      Cookbook cookbook = null;
      UUID commodity = null;
      try {
        cookbook = Database.getInstance().getCookbook(
            UUID.fromString(
                req.params("cookbook")));
        commodity = UUID.fromString(req.params("commodity"));
      } catch(IllegalArgumentException e) { }

      if(null == cookbook)
        throw new EndpointException(req, "Cookbook not found.", 404);

      if(null == commodity || !cookbook.getPantry().contains(commodity))
        throw new EndpointException(req, "Commodity not in pantry.", 404);

      for(var recipe : Database.getInstance().getCookbookRecipes(cookbook.getID()))
        if(0 == recipe.getProduct().compareTo(commodity)
            || recipe.getIngredients().containsKey(commodity))
          throw new EndpointException(req, "Commodity is in use by a recipe.", 409);

      cookbook.removeFromPantry(commodity);
      Database.getInstance().setCookbook(cookbook);

      res.status(200);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Commodity removed from pantry.")
        .put("cookbook", cookbook.getID().toString());
      
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
