/*
 * Copyright (c) 2021-2023 Axonibyte Innovations, LLC. All rights reserved.
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
package com.calebpower.mc.ecoengine.db;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Handles interactions with the database.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Database {
  
  private static Database instance = null;
  
  /**
   * Retrieves the {@link Database} instance.
   *
   * @return a {@link Database} object
   */
  public static Database getInstance() {
    return instance;
  }
  
  /**
   * Sets the {@link Database} instance.
   *
   * @param instance the {@link Database} instance
   */
  public static void setInstance(Database instance) {
    Database.instance = instance;
  }
  
  private HikariConfig hikariConfig = null;
  private HikariDataSource hikariDataSource = null;
  private String dbName = null;
  private String dbPrefix = null;
  
  /**
   * Instantiates the database handler.
   *
   * @param location the address and port to which the database is bound
   * @param prefix the string to prepend to all tables
   * @param username the username portion of the database credentials
   * @param password the password portion of the database credentials
   * @param ssl {@code true} iff the database connection should be secured
   * @throws {@link SSLException} if the database connection malfunctions
   */
  public Database(String location, String prefix, String username, String password, boolean ssl) throws SQLException {
    String[] locationArgs = location.split("/");
    if(2 != locationArgs.length)
      throw new SQLException(
          "Database location must include name of database e.g. port/database)");
    
    this.dbName = locationArgs[1];
    this.dbPrefix = prefix;
    
    this.hikariConfig = new HikariConfig();
    this.hikariConfig.setDriverClassName("org.mariadb.jdbc.Driver");
    this.hikariConfig.setJdbcUrl(
        String.format(
            "jdbc:mariadb://%1$s?autoReconnect=true&serverTimezone=UTC&useSSL=%2$b",
            location,
            ssl));
    this.hikariConfig.setUsername(username);
    this.hikariConfig.setPassword(password);
    this.hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
    this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
    this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    this.hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
    this.hikariConfig.addDataSourceProperty("useLocalSessionState", "true");
    this.hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
    this.hikariConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
    this.hikariConfig.addDataSourceProperty("cacheServerConfiguration", "true");
    this.hikariConfig.addDataSourceProperty("elideSetAutoCommits", "true");
    this.hikariConfig.addDataSourceProperty("maintainTimeState", "false");
    this.hikariConfig.addDataSourceProperty("connectionTimeout", "30000");
    this.hikariConfig.addDataSourceProperty("maxLifetime", "180000");
    this.hikariConfig.addDataSourceProperty("idleTimeout", "30000");
    this.hikariConfig.addDataSourceProperty("leakDetectionThreshold", "5000");
    this.hikariDataSource = new HikariDataSource(hikariConfig);
  }
  
  /**
   * Retrieves a {@link Connection} to the database.
   *
   * @return a {@link Connection} object
   */
  public Connection getConnection() throws SQLException {
    return hikariDataSource.getConnection();
  }
  
  /**
   * Rerieves the name of the database.
   *
   * @return a string representing the name of the database
   */
  public String getName() {
    return dbName;
  }
  
  /**
   * Retrieves the global table prefix.
   *
   * @return the string to be prepended to all table names
   */
  public String getPrefix() {
    return dbPrefix;
  }
  
  /**
   * Thoroughly closes a database connection, prepared statement, and result set.
   *
   * @param con a {@link Connection} to close, or {@code null} to skip connection closure
   * @param stmt a {@link PreparedStatement to close}, or {@code null} to skip statement closure
   * @param res a {@link ResultSet} to close, or {@code null} to skip resultset closure
   */
  public void close(Connection con, PreparedStatement stmt, ResultSet res) {
    try {
      if(null != res) res.close();
    } catch(SQLException e) {
    }
    
    try {
      if(null != stmt) stmt.close();
    } catch(SQLException e) {
    }
    
    try {
      if(null != con) con.close();
    } catch(SQLException e) {
    }
  }
  
  /**
   * Sets up the database, adding in tables and otherwise running through
   * predetermined scripts.
   *
   * @throws {@link SQLException} if there's a database malfunction
   */
  public void setup() throws SQLException {
    Connection con = getConnection();
    PreparedStatement stmt = null;
    Set<String> fileList = new TreeSet<>();
    
    CodeSource src = Database.class.getProtectionDomain().getCodeSource();
    if(null != src) {
      URL jar = src.getLocation();
      try(ZipInputStream zip = new ZipInputStream(jar.openStream())) {
        ZipEntry entry = null;
        while(null != (entry = zip.getNextEntry())) {
          var file = entry.getName();
          if(file.matches("db/.*\\.sql"))
            fileList.add(file);
        }
      } catch(IOException e) {
        e.printStackTrace();
      }
    }
    
    for(var file : fileList) {
      String resource = null;
      
      try(
          BufferedReader reader = new BufferedReader(
              new InputStreamReader(
                  getClass().getClassLoader().getResourceAsStream(file),
                  StandardCharsets.UTF_8))) {
        StringBuilder resBuilder = new StringBuilder();
        for(String line; null != (line = reader.readLine()); resBuilder.append(line.trim()).append(' '));
        resource = resBuilder.deleteCharAt(resBuilder.length() - 1).toString();
        stmt = con.prepareStatement(
            resource.replace("${database}", dbName).replace("${prefix}", dbPrefix));
        stmt.execute();
      } catch(IOException e) {
        if(null == resource)
          throw new SQLException(
              "Database bootstrap scripts could not be read.");
      } finally {
        if(null != stmt)
          try {
            stmt.close();
          } catch(SQLException e) {
          }
      }
    }
  }
  
}
