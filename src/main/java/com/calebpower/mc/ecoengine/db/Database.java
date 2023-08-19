/*
 * Copyright (c) 2021-2023 Axonibyte Innovations, LLC. All rights reserved.
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.calebpower.mc.ecoengine.db.SQLBuilder.Comparison;
import com.calebpower.mc.ecoengine.db.SQLBuilder.Join;
import com.calebpower.mc.ecoengine.model.Commodity;
import com.calebpower.mc.ecoengine.model.Recipe;
import com.calebpower.mc.ecoengine.model.Cookbook;
import com.calebpower.mc.ecoengine.model.Recipe.Work;
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
    } catch(SQLException e) { }
    
    try {
      if(null != stmt) stmt.close();
    } catch(SQLException e) { }
    
    try {
      if(null != con) con.close();
    } catch(SQLException e) { }
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
          } catch(SQLException e) { }
      }
    }
  }

  /**
   * Retrieves the set of all known commodities.
   *
   * @return a {@link Set} of {@link Commodity} objects
   * @throws SQLException if a database malfunction occurs
   */
  public Set<Commodity> getCommodities() throws SQLException {
    Map<UUID, Commodity> commodities = new HashMap<>();
    Connection con = getConnection();

    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "commodity",
            "c.id",
            "c.label",
            "r.recipe")
            .tableAlias("c")
            .join(
                Join.LEFT,
                dbPrefix + "ingredient",
                "r",
                "c.id",
                "r.commodity",
                Comparison.EQUAL_TO)
            .toString());
    ResultSet res = stmt.executeQuery();
    
    while(res.next()) {
      UUID id = SQLBuilder.bytesToUUID(res.getBytes("c.id"));
      Commodity commodity = null;
      if(commodities.containsKey(id))
        commodity = commodities.get(id);
      else
        commodities.put(id, commodity = new Commodity(id, res.getString("c.label")));
      
      UUID recipe = SQLBuilder.bytesToUUID(res.getBytes("r.recipe"));
      if(null != recipe) commodity.addUsage(recipe);
    }

    final String recipeQuery = new SQLBuilder().select(
        dbPrefix + "recipe",
        "id")
      .where("product")
      .toString();
    
    for(var id : commodities.keySet()) {
      close(null, stmt, res);
      stmt = con.prepareStatement(recipeQuery);
      stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
      res = stmt.executeQuery();

      while(res.next())
        commodities.get(id).addRecipe(
            SQLBuilder.bytesToUUID(
                res.getBytes("id")));
    }

    close(con, stmt, res);
    return Set.copyOf(commodities.values());
  }

  /**
   * Retrieves a particular commodity.
   *
   * @param id the commodity's unique {@link UUID}
   * @return a {@link Commodity} object, if one exists with an ID matching the
   *         one provided, or {@code null} if no such commodity exists
   * @throws SQLException if a database malfunction occurs
   */
  public Commodity getCommodity(UUID id) throws SQLException {
    if(null == id) return null;
    
    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "commodity",
            "c.label",
            "r.recipe")
        .tableAlias("c")
        .join(
            Join.LEFT,
            dbPrefix + "ingredient",
            "r",
            "c.id",
            "r.commodity",
            Comparison.EQUAL_TO)
        .where("c.id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    ResultSet res = stmt.executeQuery();

    Commodity commodity = null;
    while(res.next()) {
      if(null == commodity)
        commodity = new Commodity(id, res.getString("c.label"));
      
      UUID recipe = SQLBuilder.bytesToUUID(res.getBytes("r.recipe"));
      if(null != recipe) commodity.addUsage(recipe);
    }

    if(null != commodity) {
      close(null, stmt, res);
      stmt = con.prepareStatement(
          new SQLBuilder().select(
              dbPrefix + "recipe",
              "id")
              .where("product")
              .toString());
      stmt.setBytes(1, SQLBuilder.uuidToBytes(commodity.getID()));
      res = stmt.executeQuery();
      
      while(res.next())
        commodity.addRecipe(
            SQLBuilder.bytesToUUID(
                res.getBytes("id")));
    }

    close(con, stmt, res);
    return commodity;
  }

  /**
   * Adds a commodity to the database if it does not already exist. Otherwise,
   * updates the existing record.
   *
   * @param commodity the {@link Commodity} object to store in the database
   * @return {@code true} if a new record was added;
   *         {@code false} if an existing record was updated
   * @throws SQLException if a database malfunction occurs
   */
  public boolean setCommodity(Commodity commodity) throws SQLException {
    Objects.requireNonNull(commodity);
    
    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().update(
            dbPrefix + "commodity",
            "label")
        .where("id")
        .toString());
    stmt.setString(1, commodity.getLabel());
    stmt.setBytes(2, SQLBuilder.uuidToBytes(commodity.getID()));

    boolean isNew = false;
    if(isNew = 0 >= stmt.executeUpdate()) {
      close(null, stmt, null);
      stmt = con.prepareStatement(
          new SQLBuilder().insert(
              dbPrefix + "commodity",
              "label",
              "id")
          .toString());
      stmt.setString(1, commodity.getLabel());
      stmt.setBytes(2, SQLBuilder.uuidToBytes(commodity.getID()));
      stmt.executeUpdate();
    }

    close(con, stmt, null);
    return isNew;
  }

  /**
   * Removes a commodity from the database.
   *
   * @param id the commodity's unique {@link UUID}
   * @return {@code true} iff the commodity was in the database and subsequently
   *         removed from the database without issue
   * @throws SQLException if a database malfunction occurs
   */
  public boolean deleteCommodity(UUID id) throws SQLException {
    if(null == id) return false;

    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().delete(
            dbPrefix + "commodity")
        .where("id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    boolean success = 0 < stmt.executeUpdate();

    close(con, stmt, null);
    return success;
  }

  /**
   * Retrieves the set of all known recipes associated with a particular
   * cookbook.
   *
   * @param cookbook the cookbook's unique {@link UUID}
   * @return a {@link Set} of {@link Recipe} objects
   * @throws SQLException if a database error occurs
   */
  public Set<Recipe> getCookbookRecipes(UUID cookbook) throws SQLException {
    if(null == cookbook) return new HashSet<>();
    
    Map<UUID, Recipe> recipes = new HashMap<>();
    Connection con = getConnection();
    
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "recipe",
            "r.id",
            "r.product",
            "r.product_yield",
            "r.work_method",
            "r.work_cost",
            "i.commodity",
            "i.amount")
        .tableAlias("r")
        .join(
            Join.LEFT,
            dbPrefix + "ingredient",
            "i",
            "r.id",
            "i.recipe",
            Comparison.EQUAL_TO)
        .where("r.cookbook")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(cookbook));
    ResultSet res = stmt.executeQuery();

    while(res.next()) {
      UUID id = SQLBuilder.bytesToUUID(res.getBytes("r.id"));
      Recipe recipe = null;
      if(recipes.containsKey(id))
        recipe = recipes.get(id);
      else recipes.put(
          id,
          recipe = new Recipe(
              id,
              cookbook,
              SQLBuilder.bytesToUUID(
                  res.getBytes("product")),
              res.getInt("product_yield"),
              Work.values()[res.getInt("work_method")],
              res.getFloat("work_cost")));

      UUID commodity = SQLBuilder.bytesToUUID(res.getBytes("i.commodity"));
      if(null != commodity) recipe.setIngredient(commodity, res.getInt("amount"));
    }

    close(con, stmt, res);
    return Set.copyOf(recipes.values());
  }

  /**
   * Retrieves a particular recipe.
   *
   * @param id the recipe's unique {@link UUID}
   * @return a {@link Recipe} object, if one exists with an ID matching the one
   *         provided, or {@code null} if no such recipe exists
   * @throws SQLException if a database malfunction occurs
   */
  public Recipe getRecipe(UUID id) throws SQLException {
    if(null == id) return null;
    
    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "recipe",
            "r.cookbook",
            "r.product",
            "r.product_yield",
            "r.work_method",
            "r.work_cost",
            "i.commodity",
            "i.amount")
        .tableAlias("r")
        .join(
            Join.LEFT,
            dbPrefix + "ingredient",
            "i",
            "r.id",
            "i.recipe",
            Comparison.EQUAL_TO)
        .where("r.id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    ResultSet res = stmt.executeQuery();

    Recipe recipe = null;
    while(res.next()) {
      if(null == recipe)
        recipe = new Recipe(
            id,
            SQLBuilder.bytesToUUID(
                res.getBytes("cookbook")),
            SQLBuilder.bytesToUUID(
                res.getBytes("product")),
            res.getInt("product_yield"),
            Work.values()[res.getInt("work_method")],
            res.getFloat("work_cost"));

      UUID commodity = SQLBuilder.bytesToUUID(res.getBytes("i.commodity"));
      if(null != commodity) recipe.setIngredient(commodity, res.getInt("amount"));
    }

    close(con, stmt, res);
    return recipe;
  }

  /**
   * Adds a recipe to the database if it does not already exist. Otherwise,
   * updates the existing record.
   *
   * @param recipe the {@link Recipe} object to store in the database
   * @return {@code true} if a new record was added;
   *         {@code false} if an existing record was updated
   * @throws SQLException if a database malfunction occurs
   */
  public boolean setRecipe(Recipe recipe) throws SQLException {
    Objects.requireNonNull(recipe);
    byte[] idBytes = SQLBuilder.uuidToBytes(recipe.getID());

    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().update(
            dbPrefix + "recipe",
            "cookbook",
            "product",
            "product_yield",
            "work_method",
            "work_cost")
        .where("id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(recipe.getCookbook()));
    stmt.setBytes(2, SQLBuilder.uuidToBytes(recipe.getProduct()));
    stmt.setInt(3, recipe.getYield());
    stmt.setInt(4, recipe.getWork().ordinal());
    stmt.setFloat(5, recipe.getCost());
    stmt.setBytes(6, idBytes);

    Map<UUID, Integer> ingredients = null;

    boolean isNew = false;
    if(isNew = 0 >= stmt.executeUpdate()) {
      close(null, stmt, null);
      stmt = con.prepareStatement(
          new SQLBuilder().insert(
              dbPrefix + "recipe",
              "cookbook",
              "product",
              "product_yield",
              "work_method",
              "work_amount",
              "id")
          .toString());
      stmt.setBytes(1, SQLBuilder.uuidToBytes(recipe.getCookbook()));
      stmt.setBytes(2, SQLBuilder.uuidToBytes(recipe.getProduct()));
      stmt.setInt(3, recipe.getYield());
      stmt.setInt(4, recipe.getWork().ordinal());
      stmt.setFloat(5, recipe.getCost());
      stmt.setBytes(6, idBytes);
      stmt.executeUpdate();

      ingredients = recipe.getIngredients();
    } else {
      var oldIngredients = getRecipe(recipe.getID()).getIngredients();
      Set<UUID> staleIngredients = new HashSet<>();
      ingredients = new HashMap<>();

      for(var ingredient : recipe.getIngredients().entrySet())
        if(!oldIngredients.containsKey(ingredient.getKey()))
          ingredients.put(ingredient.getKey(), ingredient.getValue());
      
      for(var ingredient : oldIngredients.entrySet())
        if(!recipe.getIngredients().containsKey(ingredient.getKey()))
          staleIngredients.add(ingredient.getKey());
        else if(recipe.getIngredients().get(ingredient.getKey()) != ingredient.getValue()) {
          staleIngredients.add(ingredient.getKey());
          ingredients.put(ingredient.getKey(), ingredient.getValue());
        }

      if(!staleIngredients.isEmpty()) {
        SQLBuilder rmStaleIngredientStmt = new SQLBuilder()
            .delete(dbPrefix + "ingredient")
            .where("recipe");

        for(int i = 0; i < staleIngredients.size(); i++) {
          rmStaleIngredientStmt.where("ingredient");
          if(0 == i && 1 < staleIngredients.size())
            rmStaleIngredientStmt.or();
        }

        close(null, stmt, null);
        stmt = con.prepareStatement(rmStaleIngredientStmt.toString());
        stmt.setBytes(1, idBytes);
        int idx = 1;
        for(var stale : staleIngredients)
          stmt.setBytes(++idx, SQLBuilder.uuidToBytes(stale));

        stmt.executeUpdate();
      }
    }

    if(!ingredients.isEmpty()) {
      String addIngredientStmt = new SQLBuilder()
        .insert(
            dbPrefix + "ingredient",
            "recipe",
            "commodity",
            "amount")
        .toString();
      for(var ingredient : ingredients.entrySet()) {
        close(null, stmt, null);
        stmt = con.prepareStatement(addIngredientStmt);
        stmt.setBytes(1, idBytes);
        stmt.setBytes(2, SQLBuilder.uuidToBytes(ingredient.getKey()));
        stmt.setInt(3, ingredient.getValue());
        stmt.executeUpdate();
      }
    }

    close(con, stmt, null);
    return isNew;
  }

  /**
   * Removes a recipe from the database.
   *
   * @param id the recipe's unique {@link UUID}
   * @return {@code true} iff the recipe was in the database and subsequently
   *         removed from the database without issue
   * @throws SQLException if a database malfunction occurs
   */
  public boolean deleteRecipe(UUID id) throws SQLException {
    if(null == id) return false;
    
    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().delete(
            dbPrefix + "recipe")
        .where("id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    boolean success = 0 < stmt.executeUpdate();

    close(con, stmt, null);
    return success;
  }

  /**
   * Retrieves the set of all known cookbooks.
   *
   * @return a {@link Set} of {@link Cookbook} objects
   * @throws SQLException if a database malfunction occurs
   */
  public Set<Cookbook> getCookbooks() throws SQLException {
    Map<UUID, Cookbook> cookbooks = new HashMap<>();
    Connection con = getConnection();
    
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "cookbook",
            "w.id",
            "w.parent",
            "w.description",
            "w.creation_time",
            "w.last_update",
            "c.commodity")
        .tableAlias("w")
        .join(
            Join.LEFT,
            dbPrefix + "pantry",
            "c",
            "w.id",
            "c.cookbook",
            Comparison.EQUAL_TO)
        .toString());
    ResultSet res = stmt.executeQuery();

    while(res.next()) {
      UUID id = SQLBuilder.bytesToUUID(res.getBytes("w.id"));
      Cookbook cookbook = null;
      if(cookbooks.containsKey(id))
        cookbook = cookbooks.get(id);
      else
        cookbooks.put(
            id,
            cookbook = new Cookbook(
                id,
                SQLBuilder.bytesToUUID(
                    res.getBytes("w.parent")),
                res.getString("w.description"),
                res.getTimestamp("w.creation_time"),
                res.getTimestamp("w.last_update")));

      UUID commodity = SQLBuilder.bytesToUUID(res.getBytes("c.commodity"));
      if(null != commodity) cookbook.addSupportedCommodity(commodity);
    }

    if(!cookbooks.isEmpty()) {
      SQLBuilder getChildrenStmt = new SQLBuilder()
        .select(dbPrefix + "cookbook", "id", "parent");

      for(int i = 0; i < cookbooks.size(); i++) {
        getChildrenStmt.where("parent");
        if(0 == i && 1 < cookbooks.size())
          getChildrenStmt.or();
      }

      close(null, stmt, res);
      stmt = con.prepareStatement(getChildrenStmt.toString());

      int idx = 0;
      for(var cookbook : cookbooks.entrySet())
        stmt.setBytes(++idx, SQLBuilder.uuidToBytes(cookbook.getKey()));

      res = stmt.executeQuery();

      while(res.next())
        cookbooks.get(
            SQLBuilder.bytesToUUID(
                res.getBytes("parent")))
          .addChild(
              SQLBuilder.bytesToUUID(
                  res.getBytes("id")));
    }

    close(con, stmt, res);
    return Set.copyOf(cookbooks.values());
  }

  /**
   * Retrieves a particular cookbook.
   *
   * @param id the cookbook's unique {@link UUID}
   * @return a {@link Cookbook} object, if one exists with an ID matching the
   *         one provided, or {@code null} if no such cookbook exists
   * @throws SQLException if a database malfunction occurs
   */
  public Cookbook getCookbook(UUID id) throws SQLException {
    if(null == id) return null;

    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "cookbook",
            "w.parent",
            "w.description",
            "w.creation_time",
            "w.last_update",
            "c.commodity")
        .tableAlias("w")
        .join(
            Join.LEFT,
            dbPrefix + "pantry",
            "c",
            "w.id",
            "c.cookbook",
            Comparison.EQUAL_TO)
        .where("w.id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
    ResultSet res = stmt.executeQuery();

    Cookbook cookbook = null;
    while(res.next()) {
      if(null == cookbook)
        cookbook = new Cookbook(
            id,
            SQLBuilder.bytesToUUID(
                res.getBytes("w.parent")),
            res.getString("w.description"),
            res.getTimestamp("w.creation_time"),
            res.getTimestamp("w.last_update"));

      UUID commodity = SQLBuilder.bytesToUUID(res.getBytes("c.commodity"));
      if(null != commodity) cookbook.addSupportedCommodity(commodity);
    }

    if(null != cookbook) {
      stmt = con.prepareStatement(
          new SQLBuilder().select(
              dbPrefix + "cookbook",
              "id")
          .where("parent")
          .toString());
      stmt.setBytes(1, SQLBuilder.uuidToBytes(id));
      res = stmt.executeQuery();

      while(res.next())
        cookbook.addChild(
            SQLBuilder.bytesToUUID(
                res.getBytes("id")));
    }

    close(con, stmt, res);
    return cookbook;
  }

  /**
   * Adds a commodity to the database if it does not already exist. Otherwise,
   * updates the existing record.
   *
   * @param cookbook the {@link Cookbook} object to store in the database
   * @return {@code true} if a new record was added;
   *         {@code false} if an existing record was updated
   * @throws SQLException if a database malfunction occurs
   */
  public boolean setCookbook(Cookbook cookbook) throws SQLException {
    Objects.requireNonNull(cookbook);
    byte[] idBytes = SQLBuilder.uuidToBytes(cookbook.getID());

    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().update(
            dbPrefix + "cookbook",
            "parent",
            "description")
        .where("id")
        .toString());
    stmt.setBytes(1, SQLBuilder.uuidToBytes(cookbook.getParent()));
    stmt.setString(2, cookbook.getDescription());
    stmt.setBytes(3, idBytes);

    Set<UUID> commodities = null;

    boolean isNew = false;
    if(isNew = 0 >= stmt.executeUpdate()) {
      close(null, stmt, null);
      stmt = con.prepareStatement(
          new SQLBuilder().insert(
              dbPrefix + "cookbook",
              "parent",
              "description",
              "id")
          .toString());
      stmt.setBytes(1, SQLBuilder.uuidToBytes(cookbook.getParent()));
      stmt.setString(2, cookbook.getDescription());
      stmt.setBytes(3, idBytes);
      stmt.executeUpdate();

      commodities = cookbook.getSupportedCommodities();
    } else {
      var oldCommodities = getCookbook(cookbook.getID()).getSupportedCommodities();
      Set<UUID> staleCommodities = new HashSet<>();
      commodities = new HashSet<>();

      for(var commodity : cookbook.getSupportedCommodities()) {
        if(!oldCommodities.contains(commodity))
          commodities.add(commodity);
      }

      for(var commodity : oldCommodities)
        if(!cookbook.getSupportedCommodities().contains(commodity))
          staleCommodities.add(commodity);

      if(!staleCommodities.isEmpty()) {
        SQLBuilder rmStaleCommodityStmt = new SQLBuilder()
          .delete(dbPrefix + "pantry")
          .where("cookbook");

        for(int i = 0; i < staleCommodities.size(); i++) {
          rmStaleCommodityStmt.where("commodity");
          if(0 == i && 1 < staleCommodities.size())
            rmStaleCommodityStmt.or();
        }

        close(null, stmt, null);
        stmt = con.prepareStatement(rmStaleCommodityStmt.toString());
        stmt.setBytes(1, idBytes);
        int idx = 1;
        for(var stale : staleCommodities)
          stmt.setBytes(++idx, SQLBuilder.uuidToBytes(stale));

        stmt.executeUpdate();
      }
    }

    if(!commodities.isEmpty()) {
      String addCommodityStmt = new SQLBuilder()
        .insert(
            dbPrefix + "pantry",
            "cookbook",
            "commodity")
        .toString();
      for(var commodity : commodities) {
        close(null, stmt, null);
        stmt = con.prepareStatement(addCommodityStmt);
        stmt.setBytes(1, idBytes);
        stmt.setBytes(2, SQLBuilder.uuidToBytes(commodity));
        stmt.executeUpdate();
      }
    }

    close(con, stmt, null);
    return isNew;
  }

  /**
   * Removes a cookbook from the database.
   *
   * @param id the cookbook's unique {@link UUID}
   * @return {@code true} iff the cookbook was in the database and subsequently
   *         removed from the database without issue
   * @throws SQLException if a database malfunction occurs
   */
  public boolean deleteCookbook(UUID id) throws SQLException {
    if(null == id) return false;
    byte[] idBytes = SQLBuilder.uuidToBytes(id);

    Connection con = getConnection();
    PreparedStatement stmt = con.prepareStatement(
        new SQLBuilder().select(
            dbPrefix + "cookbook",
            "parent")
        .where("id")
        .limit(1)
        .toString());
    stmt.setBytes(1, idBytes);
    ResultSet res = stmt.executeQuery();

    if(res.next()) {
      byte[] parent = res.getBytes("parent");
      close(null, stmt, res);
      
      stmt = con.prepareStatement(
          new SQLBuilder().update(
              dbPrefix + "cookbook",
              "parent")
          .where("parent")
          .toString());
      stmt.setBytes(1, parent);
      stmt.setBytes(2, idBytes);
      stmt.executeUpdate();

      close(null, stmt, null);
    } else close(null, stmt, res);

    stmt = con.prepareStatement(
        new SQLBuilder().delete(
            dbPrefix + "cookbook")
        .where("id")
        .toString());
    stmt.setBytes(1, idBytes);
    boolean success = 0 < stmt.executeUpdate();

    close(con, stmt, null);
    return success;
  }
  
}
