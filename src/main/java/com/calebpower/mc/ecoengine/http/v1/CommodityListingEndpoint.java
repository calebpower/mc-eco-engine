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
import java.util.stream.Collector;

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

public class CommodityListingEndpoint extends JSONEndpoint {

  public CommodityListingEndpoint() {
    super("/commodities", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONArray commodityArr = Database.getInstance()
        .getCommodities()
        .stream()
        .map(c -> new JSONObject()
             .put("id", c.getID().toString())
             .put("label", c.getLabel()))
        .collect(
            Collector.of(
                JSONArray::new,
                JSONArray::put,
                JSONArray::put));

      res.status(200);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved commodities.")
        .put("commodities", commodityArr);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
