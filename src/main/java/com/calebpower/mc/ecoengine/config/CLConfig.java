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

/**
 * Parses a configuration set from command line arguments (or, more abstractly,
 * an array of ordered String objects).
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class CLConfig extends Config {

  /**
   * Loads configuration settings from an ordered array of arguments.
   *
   * @param args the arguments
   * @throws CommandArgException if a parameter and/or argument are invalid or
   *         otherwise out of order in some fashion
   */
  public void loadArgs(String[] args) throws CommandArgException {
    configVals.clear();
    ConfigParam wipParam = null;

    for(int i = 0; i < args.length; i++) {
      String candidate = args[i];

      if(wipParam == null) {
        if(!candidate.startsWith("--"))
          throw new CommandArgException(i, candidate, "Orphaned argument.");
        wipParam = ConfigParam.getParam(candidate.substring(2));
        if(wipParam == null)
          throw new CommandArgException(i, candidate, "Invalid parameter.");
      } else {
        if(configVals.containsKey(wipParam))
          throw new CommandArgException(i - 1, args[i - 1], "Duplicate parameter.");
        configVals.putIfAbsent(wipParam, candidate);
        wipParam = null;
      }
    }

    if(wipParam != null)
      throw new CommandArgException(args.length - 1, args[args.length - 1], "Hanging parameter.");
  }

  /**
   * Denotes some exception thrown during the parsing of command-line
   * arguments.
   *
   * @author Caleb L. Power <cpower@axonibyte.com>
   */
  public final class CommandArgException extends Exception {
    private static final long serialVersionUID = 1356858316621583900L;

    private int index = -1;
    private String token = null;

    private CommandArgException(int index, String token, String message) {
      super(String.format("For token #%1$d \"%2$s\": %3$s", index, token, message));
      this.index = index;
      this.token = token;
    }

    /**
     * Retrieves the index corresponding to the token that caused this
     * exception to be thrown.
     *
     * @return a 0-indexed integer corresponding to the token
     */
    public int getIndex() {
      return index;
    }

    /**
     * The token that was the cause of this exception.
     *
     * @return a String denoting the argument token
     */
    public String getToken() {
      return token;
    }

  }

}
