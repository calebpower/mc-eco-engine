/*
 * Copyright (c) 2020-2023 Axonibyte Innovations, LLC. All rights reserved.
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

import static spark.Spark.before;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static spark.Spark.stop;

import com.calebpower.mc.ecoengine.http.v1.*;

import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * API Driver; manages RESTful and WebSocket API jSONEndpoints.
 * 
 * @author Caleb L. Power
 */
public class APIDriver implements Runnable {

  private static final AtomicReference<APIDriver> instance = new AtomicReference<>();
  private static final Logger logger = LoggerFactory.getLogger(APIDriver.class);
  private static final String staticFolder = ".";

  public static APIDriver getInstance() {
    return instance.get();
  }
  
  public static void setInstance(APIDriver driver) {
    instance.set(driver);
  }
  
  private int port; // the port that the front end should run on
  private Endpoint endpoints[] = null; // the pages that will be accessible
  private String allowedOrigins = null; // the allowed origins for CORS
  private Thread thread = null; // the thread to run the frontend
  
  /**
   * Opens the specified external port so as to launch the front end.
   * 
   * @param port the port by which the front end will be accessible
   * @param allowedOrigins the allowed origins for CORS
   */
  private APIDriver(int port, String allowedOrigins) {
    this.allowedOrigins = allowedOrigins;
    this.port = port;
    
    endpoints = new Endpoint[] {
      new CommodityCreationEndpoint(),
      new CommodityDeletionEndpoint(),
      new CommodityListingEndpoint(),
      new CommodityModificationEndpoint(),
      new CommodityRetrievalEndpoint(),
      new ConfigRetrievalEndpoint(),
      new CookbookCreationEndpoint(),
      new CookbookDeletionEndpoint(),
      new CookbookListingEndpoint(),
      new CookbookModificationEndpoint(),
      new CookbookRetrievalEndpoint(),
      new RecipeCreationEndpoint(),
      new RecipeDeletionEndpoint(),
      new RecipeListingEndpoint(),
      new RecipeModificationEndpoint(),
      new RecipeRetrievalEndpoint()
    };
    
    staticFiles.location(staticFolder); // relative to the root of the classpath
  }

  /**
   * Runs the front end in a separate thread so that it can be halted externally.
   */
  @Override public void run() {
    logger.info("Exposing API on port {}", port);
    port(port);
    
    before((req, res) -> {
      res.header("Access-Control-Allow-Origin", allowedOrigins);
      res.header("Access-Control-Allow-Methods", "DELETE, POST, GET, PATCH, PUT, OPTIONS");
      res.header("Access-Control-Allow-Headers",
          "Content-Type, "
            + "Access-Control-Allow-Headers, "
            + "Access-Control-Allow-Origin, "
            + "Access-Control-Allow-Methods, "
            + "Authorization, "
            + "X-Requested-With");
      res.header("Access-Control-Expose-Headers", "Content-Type, Content-Length");
      res.header("Content-Type", "application/json"); 
    });

    options("*", (req, res)-> {
      String accessControlRequestHeaders = req.headers("Access-Control-Request-Headers");
      if(accessControlRequestHeaders != null)
        res.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      
      String accessControlRequestMethod = req.headers("Access-Control-Request-Method");
      if(accessControlRequestMethod != null)
        res.header("Access-Control-Allow-Methods", accessControlRequestMethod);

      return "OK";
    });
    
    // iterate through initialized pages and determine the appropriate HTTP request types
    for(Endpoint endpoint : endpoints)
      for(HTTPMethod method : endpoint.getHTTPMethods())
        method.getSparkMethod().accept(endpoint.getRoute(), endpoint::onRequest);
  }
  
  /**
   * Stops the web server.
   */
  public void halt() {
    stop();
  }
  
  /**
   * Builds the frontend and launches it in a thread.
   * 
   * @param port the listening port
   * @param allowedOrigins the allowed origins for CORS
   * @return a reference to this FrontEnd object
   */
  public static APIDriver build(int port, String allowedOrigins) {
    APIDriver aPIDriver = new APIDriver(port, allowedOrigins);
    aPIDriver.thread = new Thread(aPIDriver);
    aPIDriver.thread.setDaemon(false);
    aPIDriver.thread.start();
    return aPIDriver;
  }
  
}
