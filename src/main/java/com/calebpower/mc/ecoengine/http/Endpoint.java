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

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.Request;
import spark.Response;

/**
 * Module abstract class for the easy adding of custom endpoints.
 * 
 * @author Caleb L. Power
 */
public abstract class Endpoint {

  private Logger logger = LoggerFactory.getLogger(Endpoint.class);
  private HTTPMethod[] methods = null;
  private String route = null;
  
  /**
   * Overloaded constructor to set the request type, the route, and the version.
   * 
   * @param resource the public resource
   * @param version the version of the endpoint
   * @param httpMethods the HTTP methods that can be used on the route
   */
  protected Endpoint(String resource, APIVersion version, HTTPMethod... httpMethods) {
    this(String.format("/v%1$d%2$s", version.ordinal(), resource), httpMethods);
  }
  
  /**
   * Overloaded constructor to set the request type and the route.
   * 
   * @param route the public endpoint
   * @param methods the HTTP methods that can can be used on the route
   */
  protected Endpoint(String route, HTTPMethod... methods) {
    this.route = route;
    this.methods = methods;

    String clazz = null;
    for(var elem : Thread.currentThread().getStackTrace()) {
      String[] pkgs = elem.getClassName().split("\\.");
      if(pkgs.length < 2) continue;
      String parent = pkgs[pkgs.length - 2];
      if(!parent.startsWith("v") || parent.length() != 2) continue;
      clazz = elem.getClassName();
    }
    logger = LoggerFactory.getLogger(null == clazz ? Endpoint.class.getCanonicalName() : clazz);
    
    StringBuilder methodBldr = new StringBuilder();
    if(methods.length == 0)
      logger.error(
          "WARNING: Route %1$s loaded without HTTP methods.",
          route);
    else {
      for(int i = 0; i < methods.length; i++) {
        methodBldr.append(methods[i].name());
        if(i < methods.length - 1) methodBldr.append(", ");
      }
      logger.debug(
          "Loaded route {} with HTTP method(s) {}",
          route,
          methodBldr.toString());
    }
  }
  
  /**
   * Retrieve the HTTP method types for this route.
   * 
   * @return array of type HTTPMethod
   */
  public HTTPMethod[] getHTTPMethods() {
    return methods;
  }
  
  /**
   * Retrieve the route for the module.
   * 
   * @return String representing the route to be used for the module.
   */
  public String getRoute() {
    return route;
  }
  
  /**
   * The actions that will be carried out for all routes.
   * 
   * @param req REST request
   * @param res REST response
   * @return ModelAndView containing the HTTP response (often in JSON)
   */
  public String onRequest(Request req, Response res) {
    String response = null;

    try {
      logger.info(
          "{} accessed {} {}.",
          req.ip(),
          req.requestMethod(),
          req.pathInfo());
      
      response = answer(req, res);
    } catch(EndpointException e) {
      logger.error(
          "Response code {}: {} ({})",
          e.getErrorCode(),
          e.getMessage(),
          e.toString());
      res.status(e.getErrorCode());
      if(e.getErrorCode() >= 500) {
        e.printStackTrace();
        if(null != e.getCause()) e.getCause().printStackTrace();
      }
      response = new JSONObject()
          .put("status", "error")
          .put("info", e.toString())
          .toString(2) + '\n';
    } catch(Exception e) { // if we hit this block, something has gone terribly wrong (or the developer is dumb)
      logger.error(
          "A bug was found! The exception thrown was of type {}. Additional info: {}",
          e.getClass().getSimpleName(),
          null == e.getMessage() ? "N/A" : e.getMessage());
      e.printStackTrace();
      if(null != e.getCause()) e.getCause().printStackTrace();
      res.status(500);
      response = new JSONObject()
          .put("status", "error")
          .put("info", "Internal server error.")
          .toString(2) + '\n';
    }

    return response;
  }
  
  /**
   * Generates a response to the user's request.
   * 
   * @param req HTTP request
   * @param res HTTP response
   * @return the response body
   * @throws EndpointException thrown if the request is bad
   */
  public abstract String answer(Request req, Response res) throws EndpointException;
  
  protected boolean isIPv4Addr(String candidate) {
    return null != candidate && candidate.matches("((2(5[0-5])|(4\\d))|(\\d{1,3})\\.){3}((2(5[0-5])|(4\\d))|(\\d{1,3}))");
  }

  protected String fromTimestamp(Timestamp timestamp) {
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    return sdf.format(timestamp);
  }
  
  protected Timestamp toTimestamp(String value) {
    final SimpleDateFormat[] sdfArr = {
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS"),
        new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"),
        new SimpleDateFormat("yyyy-MM-dd hh:mm"),
        new SimpleDateFormat("yyyy-MM-dd")
    };
    for(int i = 0; i < sdfArr.length; i++) {
      try {
        return new Timestamp(sdfArr[i].parse(value).getTime());
      } catch(ParseException e) { }
    }
    return null;
  }
  
  protected UUID yankIDFromJSON(JSONObject body, String key) {
    if(null == body || null == key) return null;
    String val = body.optString(key);
    try {
      return UUID.fromString(val);
    } catch(IllegalArgumentException e) { }
    return null;
  }
  
  protected UUID yankIDFromURL(Request req, String key) {
    String val = req.params(key);
    try {
      return null == val ? null : UUID.fromString(val);
    } catch(IllegalArgumentException e) { }
    return null;
  }
  
  protected UUID yankIDFromQuery(Request req, String key) {
    if(null == key) return null;
    String val = req.queryParamOrDefault(key.strip(), null);
    try {
      return null == val ? null : UUID.fromString(val);
    } catch(IllegalArgumentException e) { }
    return null;
  }
  
}
