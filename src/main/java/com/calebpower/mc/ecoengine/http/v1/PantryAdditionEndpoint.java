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

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Commodity;
import com.calebpower.mc.ecoengine.model.Cookbook;

import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the addition of commodities to a cookbook's pantry.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class PantryAdditionEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public PantryAdditionEndpoint() {
    super("/cookbooks/:cookbook/pantry/:commodity", APIVersion.VERSION_1, HTTPMethod.PUT);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      Cookbook cookbook = null;
      Commodity commodity = null;
      try {
        cookbook = Database.getInstance().getCookbook(
            UUID.fromString(
                req.params("cookbook")));
        commodity = Database.getInstance().getCommodity(
            UUID.fromString(
                req.params("commodity")));
      } catch(IllegalArgumentException e) { }

      if(null == cookbook)
        throw new EndpointException(req, "Cookbook not found.", 404);

      if(null == commodity)
        throw new EndpointException(req, "Commodity not found.", 404);

      JSONObject resBody = new JSONObject()
        .put("status", "ok")
        .put("cookbook", cookbook.getID().toString())
        .put("commodity", commodity.getID().toString());
      
      if(cookbook.getPantry().contains(commodity.getID())) {
        res.status(202);
        resBody.put("info", "Commodity was already in pantry.");
      } else {
        cookbook.addToPantry(commodity.getID());
        Database.getInstance().setCookbook(cookbook);
        res.status(201);
        resBody.put("info", "Added commodity to pantry.");
      }

      return resBody;
      
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
