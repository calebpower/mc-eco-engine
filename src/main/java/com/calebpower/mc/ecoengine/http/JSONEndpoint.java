/*
 * Copyright (c) 2019-2023 Axonibyte Innovations, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.calebpower.mc.ecoengine.http;

import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Module abstract class for the easy-adding of custom JSON endpoints.
 *. 
 * @author Caleb L. Power
 */
public abstract class JSONEndpoint extends Endpoint {
  
  protected JSONEndpoint(String resource, APIVersion version, HTTPMethod... httpMethods) {
    super(resource, version, httpMethods);
  }
  
  protected JSONEndpoint(String route, HTTPMethod... methods) {
    super(route, methods);
  }
  
  @Override public String answer(Request req, Response res) throws EndpointException {
    JSONObject resBody = doEndpointTask(req, res);
    return resBody == null ? "" : (resBody.toString(2) + '\n');
  }
  
  /**
   * Generates a response to the user's request.
   * 
   * @param req HTTP request
   * @param res HTTP response
   * @return JSON response body
   * @throws EndpointException thrown if the request is bad
   */
  public abstract JSONObject doEndpointTask(Request req, Response res) throws EndpointException;
  
}
