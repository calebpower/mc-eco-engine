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

/**
 * Facilitates the listing of known cookbooks.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookListingEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CookbookListingEndpoint() {
    super("/cookbooks", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONArray cookbookArr = Database.getInstance()
          .getCookbooks()
          .stream()
          .map(w -> new JSONObject()
              .put("id", w.getID().toString())
              .put("description", w.getDescription())
              .put(
                  "parent",
                  null == w.getParent()
                      ? JSONObject.NULL
                      : w.getParent().toString()))
          .collect(
              Collector.of(
                  JSONArray::new,
                  JSONArray::put,
                  JSONArray::put));

      res.status(200);
      return new JSONObject()
        .put("status", "ok")
        .put("info", "Retrieved cookbooks.")
        .put("cookbooks", cookbookArr);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
