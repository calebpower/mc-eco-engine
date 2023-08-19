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

public class CommodityCreationEndpoint extends JSONEndpoint {

  public CommodityCreationEndpoint() {
    super("/commodities", APIVersion.VERSION_1, HTTPMethod.POST);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONObject reqBody = new JSONObject(req.body());
      String label = reqBody.getString("label");

      if(label.isBlank())
        throw new EndpointException(req, "Invalid label.", 400);

      UUID id = null;
      do id = UUID.randomUUID(); while(null != Database.getInstance().getCommodity(id));

      Commodity commodity = new Commodity(id, label.strip());
      Database.getInstance().setCommodity(commodity);

      res.status(201);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Created commodity.")
        .put("commodity", new JSONObject()
             .put("id", id.toString())
             .put("label", label));
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
