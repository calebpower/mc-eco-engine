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

import com.calebpower.mc.ecoengine.config.Config;
import com.calebpower.mc.ecoengine.config.Config.ConfigParam;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;

import org.json.JSONArray;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the retrieval of the current configuration state.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class ConfigRetrievalEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public ConfigRetrievalEndpoint() {
    super("/configs", APIVersion.VERSION_1, HTTPMethod.GET);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    JSONArray cfgOptions = new JSONArray();
    Config config = Config.getInstance();
    for(var param : ConfigParam.values())
      cfgOptions.put(
          new JSONObject()
              .put(
                  param.toString(),
                  config.canResolve(param)
                      ? config.getString(param)
                      : JSONObject.NULL));

    return new JSONObject()
      .put("status", "ok")
      .put("info", "Retrieved configs.")
      .put("configs", cfgOptions);
  }
  
}
