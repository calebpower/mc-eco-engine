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

import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Cookbook;

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates cookbook modification.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookModificationEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CookbookModificationEndpoint() {
    super("/cookbooks/:cookbook", APIVersion.VERSION_1, HTTPMethod.PATCH);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONObject reqBody = new JSONObject(req.body());

      Cookbook cookbook = null;
      try {
        cookbook = Database.getInstance().getCookbook(
            UUID.fromString(
                req.params("cookbook")));
      } catch(IllegalArgumentException e) { }

      if(null == cookbook)
        throw new EndpointException(req, "Cookbook not found.", 404);

      if(reqBody.has("description"))
        cookbook.setDescription(
            reqBody.getString("description"));

      Database.getInstance().setCookbook(cookbook);
      cookbook = Database.getInstance().getCookbook(cookbook.getID());

      final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      res.status(200);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "Modified cookbook.")
          .put("cookbook", new JSONObject()
              .put("id", cookbook.getID().toString())
              .put("description", cookbook.getDescription())
              .put(
                  "parent",
                  null == cookbook.getParent()
                      ? JSONObject.NULL
                      : cookbook.getParent().toString())
              .put("timeCreated", sdf.format(cookbook.getTimeCreated()))
              .put("timeModified", sdf.format(cookbook.getTimeModified())));
      
    } catch(JSONException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
