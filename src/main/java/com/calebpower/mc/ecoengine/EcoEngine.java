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
package com.calebpower.mc.ecoengine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.calebpower.mc.ecoengine.config.CLConfig;
import com.calebpower.mc.ecoengine.config.Config;
import com.calebpower.mc.ecoengine.config.FileConfig;
import com.calebpower.mc.ecoengine.config.Config.ConfigParam;
import com.calebpower.mc.ecoengine.config.FileConfig.FileReadException;
import com.calebpower.mc.ecoengine.db.Database;
import com.calebpower.mc.ecoengine.http.APIDriver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcoEngine {

  public static void main(String[] args) {
    final Logger logger = LoggerFactory.getLogger(EcoEngine.class);

    logger.info("Hello, world!");
    
    try {
      logger.info("Loading config...");

      Config cfg = new CLConfig();
      ((CLConfig)cfg).loadArgs(args);

      if(cfg.canResolve(ConfigParam.CONFIG)) {
        String cfgFile = cfg.getString(ConfigParam.CONFIG);
        FileConfig fileConfig = new FileConfig(cfgFile);
        try {
          fileConfig.load();
          cfg = cfg.merge(fileConfig);
        } catch(FileReadException e) {
          try(InputStream in = EcoEngine.class.getResourceAsStream("/config/default.json");
              OutputStream out = new FileOutputStream(new File(cfgFile))) {
            byte[] buf = new byte[4096];
            for(int len; -1 != (len = in.read(buf));)
              out.write(buf, 0, len);
          }
          logger.error("Default config generated. Please customize it and try again.");
          System.exit(2);
        }
      }

      Config.setInstance(cfg);

      if(cfg.getBoolean(ConfigParam.LOG_DEBUG)) {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        logger.debug("Debug logs have been enabled.");
      }

      logger.info("Connecting to database...");
      Database database = new Database(
          String.format(
              "%1$s/%2$s",
              cfg.getString(ConfigParam.DB_ENDPOINT),
              cfg.getString(ConfigParam.DB_DATABASE)),
          cfg.getString(ConfigParam.DB_PREFIX),
          cfg.getString(ConfigParam.DB_USERNAME),
          cfg.getString(ConfigParam.DB_PASSWORD),
          cfg.getBoolean(ConfigParam.DB_SECURE));
      database.setup();
      Database.setInstance(database);

      logger.info("Spinning up API driver...");
      APIDriver.setInstance(
          APIDriver.build(
              cfg.getInteger(ConfigParam.API_PORT),
              cfg.getString(ConfigParam.API_ALLOWED_ORIGINS)));

      logger.info("Applying shutdown hook...");
      Runtime.getRuntime().addShutdownHook(
          new Thread() {
            @Override public void run() {
              logger.info("Shutting down...");
              APIDriver.getInstance().halt();
              logger.info("Goodbye! ^_^");
            }
          }
      );

      logger.info("Ready to go!");
      
    } catch(Exception e) {
      e.printStackTrace();
      logger.error(
          "Some exception was thrown during the launch sequence: {}",
          null == e.getMessage() ? "no further info available." : e.getMessage());
      System.exit(1);
    }

    
  }
  
}
