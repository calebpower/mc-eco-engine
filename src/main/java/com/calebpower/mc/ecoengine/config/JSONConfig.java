/*
 * Copyright (c) 2019-2022 Axonibyte Innovations, LLC. All rights reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.calebpower.mc.ecoengine.config;

import org.json.JSONObject;

/**
 * Driver to obtain configuration values from a JSON object.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class JSONConfig extends Config {

  /**
   * Instantiates a JSONConfig object.
   */
  public JSONConfig() {
    super();
  }

  /**
   * Instantiates a JSONConfig object with preloaded data.
   *
   * @param config the data to load on instantiation
   */
  public JSONConfig(Config config) {
    super(config);
  }

  /**
   * Deserializes JSON into a working config.
   *
   * @param jso the JSON object to deserialize
   */
  public void deserialize(JSONObject jso) {
    for(var param : ConfigParam.values()) {
      var arg = jso.optQuery(toPointer(param.toString()));
      if(arg != null) configVals.put(param, arg);
    }
  }

  /**
   * Serializes the config into a JSONObject.
   * Note: this method only supports nested JSON objects.
   *
   * @return a JSONObject containing configuration data
   */
  public JSONObject serialize() {
    JSONObject serialized = new JSONObject();

    configVals.forEach((k, v) -> {
        String[] keys = k.toString().split("\\.");
        Object obj = keys[keys.length - 1];
        for(int i = keys.length - 1; i > 0; i--) {
          JSONObject jso = new JSONObject().put(keys[i], obj);
          obj = jso;
        }
        serialized.put(keys[0], obj);
      });

    return serialized;
  }

  private String toPointer(String path) {
    return '/' + path.replace('.', '/');
  }

}
