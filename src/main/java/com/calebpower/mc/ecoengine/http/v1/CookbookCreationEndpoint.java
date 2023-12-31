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
import com.calebpower.mc.ecoengine.model.Recipe;

import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the creation of a cookbook.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookCreationEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CookbookCreationEndpoint() {
    super("/cookbooks", APIVersion.VERSION_1, HTTPMethod.POST);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      JSONObject reqBody = new JSONObject(req.body());
      
      Cookbook parent = null;
      if(reqBody.has("parent")) {
        try {
          parent = Database.getInstance().getCookbook(
              UUID.fromString(
                  reqBody.getString("parent")));
        } catch(IllegalArgumentException e) { }

        if(null == parent)
          throw new EndpointException(req, "Parent not found.", 404);
      }

      String description = null;
      if(reqBody.has("description"))
        description = reqBody.getString("description").strip();

      UUID id = null;
      do id = UUID.randomUUID(); while(null != Database.getInstance().getCookbook(id));

      Cookbook cookbook = new Cookbook(id, parent, description);
      Database.getInstance().setCookbook(cookbook);
      cookbook = Database.getInstance().getCookbook(id);
      
      if(null != parent) {
        for(var r : Database.getInstance().getCookbookRecipes(parent.getID())) {
          UUID rID = null;
          do id = UUID.randomUUID(); while(null != Database.getInstance().getRecipe(id));
          Database.getInstance().setRecipe(new Recipe(rID, r));
        }
      }

      final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      res.status(201);
      return new JSONObject()
          .put("status", "ok")
          .put("info", "Created cookbook.")
          .put("cookbook", new JSONObject()
              .put("id", id.toString())
              .put("description", cookbook.getDescription())
              .put(
                  "parent",
                  null == cookbook.getParent()
                      ? JSONObject.NULL
                      : cookbook.getParent().toString())
              .put("timeCreated", sdf.format(cookbook.getTimeCreated())));
      
    } catch(IllegalArgumentException | JSONException e) {
      throw new EndpointException(req, "Syntax error.", 400, e);
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }
  
}
