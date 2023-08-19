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
import com.calebpower.mc.ecoengine.model.Commodity;

import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the retrieval of individual global commodities.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CommodityRetrievalEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CommodityRetrievalEndpoint() {
    super("/commodities/:commodity", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    Commodity commodity = null;
    try {
      commodity = Database.getInstance().getCommodity(
          UUID.fromString(
              req.params("commodity")));
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    } catch(IllegalArgumentException e) { }

    if(null == commodity)
      throw new EndpointException(req, "Commodity not found.", 404);

    final Collector<String, JSONArray, JSONArray> toJSONArr = Collector.of(
        JSONArray::new,
        JSONArray::put,
        JSONArray::put);

    res.status(200);
    return new JSONObject()
      .put("status", "ok")
      .put("info", "Retrieved commodity.")
      .put("commodity", new JSONObject()
           .put("id", commodity.getID().toString())
           .put("label", commodity.getLabel())
           .put("recipes", commodity.getRecipes()
                .stream()
                .map(r -> r.toString())
                .collect(toJSONArr))
           .put("usages", commodity.getUsages()
                .stream()
                .map(u -> u.toString())
                .collect(toJSONArr)));
  }
  
}
