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
import java.util.Objects;
import java.util.UUID;

import com.calebpower.mc.ecoengine.analyzer.Analysis;
import com.calebpower.mc.ecoengine.analyzer.CookbookAnalyzer;
import com.calebpower.mc.ecoengine.analyzer.Diff;
import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIVersion;
import com.calebpower.mc.ecoengine.http.EndpointException;
import com.calebpower.mc.ecoengine.http.HTTPMethod;
import com.calebpower.mc.ecoengine.http.JSONEndpoint;
import com.calebpower.mc.ecoengine.model.Cookbook;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import spark.Request;
import spark.Response;

/**
 * Facilitates the submission of an cookbook analysis batch.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CookbookAnalysisEndpoint extends JSONEndpoint {

  /**
   * Instantiates the endpoint.
   */
  public CookbookAnalysisEndpoint() {
    super("/cookbooks/:cookbook/analysis", APIVersion.VERSION_1, HTTPMethod.POST);
  }

  @Override public JSONObject doEndpointTask(Request req, Response res) throws EndpointException {
    try {
      Cookbook cookbook = null;
      
      try {
        cookbook = Database.getInstance().getCookbook(
            UUID.fromString(
                req.params("cookbook")));
      } catch(IllegalArgumentException e) { }

      if(null == cookbook)
        throw new EndpointException(req, "Cookbook not found.", 404);

      Cookbook minuend = null;
      
      try {
        JSONObject reqBody = new JSONObject(req.body());
        if(reqBody.has("minuend")) {
          String minuendID = reqBody.optString("minuend", null);
          if(null != minuendID) {
            minuend = Database.getInstance().getCookbook(
                UUID.fromString(minuendID));
          }
          if(null == minuend)
            throw new EndpointException(req, "Comparison cookbook not found.", 404);
        }
      } catch(IllegalArgumentException | JSONException e) { }

      JSONArray commodityArr = new JSONArray();
      JSONObject resBody = new JSONObject().put("status", "ok");

      if(null == minuend) { // analysis only

        Analysis analysis = new CookbookAnalyzer(cookbook).analyze();
        for(var commodity : cookbook.getPantry())
          commodityArr.put(
              serializeAnalysis(commodity, analysis)
                  .put("id", commodity));

        resBody
          .put("info", "Computed analysis.")
          .put("cookbook", cookbook.getID().toString());
        
      } else { // difference between the two

        Diff diff = new CookbookAnalyzer(cookbook).diff(minuend);
        for(var commodity : diff.getCommodityUnion()) {
          JSONObject commodityObj = new JSONObject().put("id", commodity);
          if(diff.getModifiedCommodities().containsKey(commodity)) {
            commodityObj
                .put(
                    "firstAnalysis",
                    serializeAnalysis(commodity, diff.getFirstAnalysis()))
                .put(
                    "secondAnalysis",
                    serializeAnalysis(commodity, diff.getSecondAnalysis()))
                .put("status", "MODIFIED")
                .put("diff", diff.getModifiedCommodities().get(commodity));
          } else if(diff.getDisappearingCommodities().contains(commodity)) {
            commodityObj
                .put(
                    "firstAnalysis",
                    serializeAnalysis(commodity, diff.getFirstAnalysis()))
                .put("secondAnalysis", JSONObject.NULL)
                .put("status", "DISAPPEARING");
          } else if(diff.getNewCommodities().contains(commodity)) {
            commodityObj
                .put("firstAnalysis", JSONObject.NULL)
                .put(
                    "secondAnalysis",
                    serializeAnalysis(commodity, diff.getSecondAnalysis()))
                .put("status", "NEW");
          }
          
          commodityArr.put(commodityObj);
        }

        resBody
          .put("info", "Computed diff.")
          .put("minuend", minuend.getID().toString())
          .put("subtrahend", cookbook.getID().toString());
      }

      return resBody
        .put("commodities", commodityArr);
      
    } catch(SQLException e) {
      throw new EndpointException(req, "Database malfunction.", 503, e);
    }
  }

  private JSONObject serializeAnalysis(UUID commodity, Analysis analysis) {
    Objects.requireNonNull(analysis);
    
    JSONObject commodityObj = new JSONObject();
    if(analysis.isFungible(commodity))
      commodityObj.put("status", "FUNGIBLE")
          .put("value", analysis.getFungibleCommodities().get(commodity));
    else commodityObj.put("status", "NONFUNGIBLE");
    
    return commodityObj;
  }
  
}
