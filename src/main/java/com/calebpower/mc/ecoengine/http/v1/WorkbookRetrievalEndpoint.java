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
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.stream.Collector;

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Cookbook;

import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates cookbook retrieval.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookRetrievalEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CookbookRetrievalEndpoint() {
    super("/cookbooks/:cookbook", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    Cookbook cookbook = null;
    try {
      cookbook = Database.getInstance().getCookbook(
          UUID.fromString(
              req.params("cookbook")));
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    } catch(IllegalArgumentException e) { }

    if(null == cookbook)
      throw new EndpointException(req, "Cookbook not found.", 404);

    final Collector<String, JSONArray, JSONArray> toJSONArr = Collector.of(
        JSONArray::new,
        JSONArray::put,
        JSONArray::put);

    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    res.status(200);
    return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved cookbook.")
        .put("cookbook", new JSONObject()
            .put("id", cookbook.getID().toString())
            .put("description", cookbook.getDescription())
            .put(
                "parent",
                null == cookbook.getParent()
                    ? JSONObject.NULL
                    : cookbook.getParent().toString())
            .put("children", cookbook.getChildren()
                .stream()
                .map(c -> c.toString())
                .collect(toJSONArr))
            .put("commodities", cookbook.getSupportedCommodities()
                .stream()
                .map(c -> c.toString())
                .collect(toJSONArr))
            .put("timeCreated", sdf.format(cookbook.getTimeCreated()))
            .put("timeModified", sdf.format(cookbook.getTimeModified())));
  }
  
}
