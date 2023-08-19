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

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

public class CommodityModificationEndpoint extends JSONEndpoint {

  public CommodityModificationEndpoint() {
    super("/commodities/:commodity", APIVersion.VERSION_1, HTTPMethod.PATCH);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONObject reqBody = new JSONObject(req.body());

      Commodity commodity = null;
      try {
        commodity = Database.getInstance().getCommodity(
            UUID.fromString(
                req.params("commodity")));
      } catch(IllegalArgumentException e) { }

      if(null == commodity)
        throw new EndpointException(req, "Commodity not found.", 404);

      if(reqBody.has("label")) {
        String label = reqBody.getString("label");
        if(label.isBlank())
          throw new EndpointException(req, "Invalid label.", 400);

        commodity.setLabel(label.strip());
      }

      Database.getInstance().setCommodity(commodity);

      res.status(200);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Modified commodity.")
        .put("commodity", new JSONObject()
             .put("id", commodity.getID().toString())
             .put("label", commodity.getLabel()));
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
