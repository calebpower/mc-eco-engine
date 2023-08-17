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
 * See the icense for the specific language governing permissions and
 * imitations under the License.
 */
package com.calebpower.mc.ecoengine.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.json.JSONArray;

/**
 * An overloadable configuration driver.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Config {

  /**
   * Denotes the set of possible configuration parameters.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public static enum ConfigParam {

    /**
     * Denotes the path of the configuration file.
     */
    CONFIG("config"),

    /**
     * Denotes the port that the API endpoints will be exposed to.
     */
    API_PORT("api.port"),

    /**
     * Denotes the CORS origin settings for the API.
     */
    API_ALLOWED_ORIGINS("api.allowedOrigins"),

    /**
     * Denotes the authentication requirement; first run should be false to add keys.
     */
    API_REQUIRE_AUTH("api.requireAuth", true),
    
    /**
     * Denotes the database endpoint by which it can be accessed.
     */
    DB_ENDPOINT("db.endpoint"),

    /**
     * Denotes the username used to access the database.
     */
    DB_USERNAME("db.username"),

    /**
     * Denotes the password used to access the database.
     */
    DB_PASSWORD("db.password"),

    /**
     * Denotes the name of the database that is to be used.
     */
    DB_DATABASE("db.database"),

    /**
     * Denotes the prefix to prepend to table names in the database.
     */
    DB_PREFIX("db.prefix"),

    /**
     * Denotes whether or not the database connection should be encrypted.
     */
    DB_SECURE("db.secure"),
    
    /**
     * Denotes whether or not debug logs should be displayed.
     */
    LOG_DEBUG("log.debug", false);

    /**
     * Retrieves a {@link ConfigParam} enumerabe object based on its path.
     *
     * @param string the path or key associated with the enum object
     * @return the {@link ConfigParam} object, if the needle matches, or {@code null}
     */
    public static ConfigParam getParam(String string) {
      String needle = string.strip();
      for(int i = 0; i < values().length; i++)
        if(values()[i].path.equalsIgnoreCase(needle))
          return values()[i];
      return null;
    }

    private String path = null;
    private Object detour = null;

    private ConfigParam(String path) {
      this.path = path;
    }

    private ConfigParam(String path, Object detour) {
      this(path);
      this.detour = detour;
    }

    /**
     * Retrieves the parameter that should be queried if this argument is not
     * properly defined. Alternatively, defines some default value.
     *
     * If this method returns {@code null}, then there is no detour.
     *
     * @return the next place to look if this argument fails
     */
    public Object getDetour() {
      return detour;
    }

    @Override public String toString() {
      return path;
    }
  }

  private static AtomicReference<Config> instance = new AtomicReference<>();

  /**
   * Retrieves the global config instance.
   *
   * @return a {@link Config} object
   */
  public static Config getInstance() {
    return instance.get();
  }

  /**
   * Sets the global config instance.
   *
   * @param instance the desired {@link Config} object
   */
  public static void setInstance(Config instance) {
    Config.instance.set(instance);
  }
  
  protected final Map<ConfigParam, Object> configVals = new HashMap<>();
  
  protected Config() { }
  
  protected Config(Config config) {
    config.configVals.forEach((k, v) -> configVals.put(k, v));
  }
  
  /**
   * Resolves a configuration parameter into its respective argument, its
   * default (if such an argument does not exist), or the argument of its
   * detoured parameter, if one has been specified.
   *
   * @param param the parameter to query
   * @return the argument, if one exists; otherwise, {@code null}
   * @throws {@link BadParamException} if there was no argument or appropriate detour
   */
  public Object resolve(ConfigParam param) throws BadParamException {
    var arg = resolve(param, 0);
    if(null == arg) throw new BadParamException(param);
    return arg;
  }

  private Object resolve(ConfigParam param, int depth) {
    if(depth > ConfigParam.values().length) return null; // prevent stack overflow
    if(configVals.containsKey(param)) return configVals.get(param); // direct value
    var detour = param.getDetour();
    return (detour instanceof ConfigParam) ? resolve((ConfigParam)detour, depth + 1) : detour;
  }

  /**
   * Determines whether or not a parameter can be resolved to an argument.
   *
   * @param the parameter to query
   * @return {@code true} iff the parameter can be resolved; that is,
   *         {@code true} iff {@link Config#resolve(ConfigParam)} would not
   *         throw a {@link BadParamException}, provided that this Config
   *         option is assumed to be atomic
   */
  public boolean canResolve(ConfigParam param) {
    return null != resolve(param, 0);
  }

  /**
   * Retrieves the String value of the requested config value.
   *
   * @param param the configuration parameter
   * @return a String denoting the requested configuration value
   * @throws {@link BadParamException} iff the argument that corresponds with the
   *         provided parameter is either undefined or {@code null}
   */
  public String getString(ConfigParam param) throws BadParamException {
    return String.valueOf(resolve(param));
  }

  /**
   * Retrieves the char value of the requested config option.
   *
   * @param the configuration parameter
   * @return a char denoting the value of the requested config option
   * @throws {@link BadParamException} if the value that corresponds with the provided
   *         parameter is undefined or {@code null}, or if said value could not be
   *         converted to a char
   */
  public char getChar(ConfigParam param) throws BadParamException {
    var arg = getString(param);
    if(1 != arg.length()) throw new BadParamException(param);
    return arg.charAt(0);
  }

  /**
   * Retrieves the boolean value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a boolena denoting the vaue of the requested config option
   * @throws {@link BadParamException} if the value that correponds with
   *         the provided parameter is undefined or {@code null}
   */
  public boolean getBoolean(ConfigParam param) throws BadParamException {
    return Boolean.parseBoolean(getString(param));
  }

  /**
   * Retrieves the integer value of the requested config option.
   *
   * @param param the configuration parameter
   * @return an integer denoting the value of the requested config option
   * @throws {@link BadParamException} if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or if
   *         said value could not be converted to an integer
   */
  public int getInteger(ConfigParam param) throws BadParamException {
    try {
      return Integer.parseInt(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the long value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a long datum denoting the value of the requested config option
   * @throws {@BadParamException} if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or
   *         if said value could not be converted to a long datum
   */
  public long getLong(ConfigParam param) throws BadParamException {
    try {
      return Long.parseLong(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the double value of the requested config option.
   *
   * @param param the configuration parameter
   * @return a double datum denoting the value of the requested config option
   * @throws {@link BadParamException} if the value that corresponds with
   *         the provided parameter is undefined or {@code null}, or if
   *         said value could not be converted to a double datum
   */
  public double getDouble(ConfigParam param) throws BadParamException {
    try {
      return Double.parseDouble(getString(param));
    } catch(NumberFormatException e) {
      throw new BadParamException(param);
    }
  }

  /**
   * Retrieves the JSONArray associated with the requested config option.
   *
   * @param param the configuration parameter
   * @return a JSONArray containing a list of objects
   * @throws {@link BadparamException} if the value that corresponds with
   *         the provided parameter is not of type {@link JSONArray}
   */
  public JSONArray getArr(ConfigParam param) throws BadParamException {
    Object arr = resolve(param);
    if(!(arr instanceof JSONArray)) throw new BadParamException(param);
    return (JSONArray)arr;
  }

  /**
   * Merges this config and another config into a new Config object. Note that
   * neither this Config object nor the Config argument are mutated via this
   * procedure. Furthermore, the values of the Config argument supersede that
   * of the values of this object, unless the value of the Config argument is
   * {@code null} or is not set, in which case the value of this Config object
   * remains.
   *
   * @param config the config that should be merged into this one
   * @return a new Config representation of the two merged configurations
   */
  public Config merge(Config config) {
    Config merger = new Config(this);
    config.configVals.forEach((k, v) -> {
        if(null != v) {
          if(merger.configVals.containsKey(k))
            merger.configVals.replace(k, v);
          else merger.configVals.putIfAbsent(k, v);
        }
      });
    return merger;
  }

  /**
   * An Exception that is thrown if an argument could not be retrieved. This is
   * most likely to be thrown if the configuration argument corresponding to
   * the specified parameter is not defined.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public final class BadParamException extends RuntimeException {
    private static final long serialVersionUID = 9128342008991622490L;

    /**
     * Instantiates the BadParamException.
     *
     * @param param the configuration parameter that could not be retrieved
     */
    public BadParamException(ConfigParam param) {
      super(
          String.format(
              "Argument for parameter %1$s was not defined or has the wrong type.",
              param));
    }
  }
  
}
