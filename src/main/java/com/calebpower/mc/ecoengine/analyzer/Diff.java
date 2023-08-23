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
package com.calebpower.mc.ecoengine.analyzer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Describes an analysis of the differences between two cookbooks.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Diff {

  private Analysis a1 = null, a2 = null;
  private Map<UUID, Float> modifiedCommodities = new HashMap<>();
  private Set<UUID> disappearingCommodities = new HashSet<>();
  private Set<UUID> newCommodities = new HashSet<>();

  /**
   * Instantiates this {@link Diff} object.
   *
   * @param a1 the analysis associated with the older cookbook
   * @param a2 the analysis associated with the newer cookbook
   */
  public Diff(Analysis a1, Analysis a2) {
    Objects.requireNonNull(a1);
    Objects.requireNonNull(a2);
    this.a1 = a1;
    this.a2 = a2;
  }

  /**
   * Retrieves the union of all known commodities across both cookbooks
   * represented by this diff.
   *
   * @return a {@link Set} of {@link UUID} objects associated with various
   *         commodities
   */
  public Set<UUID> getCommodityUnion() {
    Set<UUID> union = new HashSet<>(a1.getFungibleCommodities().keySet());
    union.addAll(a1.getNonfungibleCommodities());
    union.addAll(a2.getFungibleCommodities().keySet());
    union.addAll(a2.getNonfungibleCommodities());
    return union;
  }
  
  /**
   * Retrieves the analysis associated with the older cookbook.
   *
   * @return an {@link Analysis} object associated with the older cookbook
   */
  public Analysis getFirstAnalysis() {
    return a1;
  }

  /**
   * Retrieves the analysis associated with the newer cookbook.
   *
   * @return an {@link Analysis} object associated with the newer cookbook
   */
  public Analysis getSecondAnalysis() {
    return a2;
  }

  /**
   * Retrieves a map of commodity identifiers and the differences in value
   * between the commodities as they exist in the two cookbooks. A positive
   * value indicates a net increase in value, and a negative value indicates
   * a net decreate in value. Obviously, {@code 0} indicates no change in value.
   *
   * Nonfungible commodities will be included in this map. If the commodity was
   * previously nonfungible and is now fungible, the diff will be the positive
   * value of the second instance. If the commodity was previously fungible and
   * is now fungible, the diff will be the negative value of the first instance.
   * If the commodity is nonfungible in both cookbooks, then then diff will be,
   * as expected, {@code 0}.
   *
   * @return a {@link Map} of commodity {@link UUID} objects and their
   *         respective {@link Float} values
   */
  public Map<UUID, Float> getModifiedCommodities() {
    return Collections.unmodifiableMap(modifiedCommodities);
  }

  /**
   * Adds a modified commodity and its respective value change.
   *
   * @param commodity the unique {@link UUID} associated with the commodity
   * @param delta the change in value
   */
  public void addModifiedCommodity(UUID commodity, float delta) {
    Objects.requireNonNull(commodity);
    if(disappearingCommodities.contains(commodity))
      throw new IllegalStateException("commodity was already declared disappearing");
    if(newCommodities.contains(commodity))
      throw new IllegalStateException("commodity was already declared new");

    modifiedCommodities.put(commodity, delta);
  }

  /**
   * Retrieves the set of commodities that were in the first cookbook but were
   * not present in the second. This represents a commodity that was removed.
   *
   * @return a {@link Set} of {@link UUID} objects associated with disappearing
   *         commodities
   */
  public Set<UUID> getDisappearingCommodities() {
    return Collections.unmodifiableSet(disappearingCommodities);
  }

  /**
   * Makes known a commodity that was in the first cookbook but was not present
   * in the second.
   *
   * @param commodity the unique {@link UUID} of the commodity that disappeared
   */
  public void addDisappearingCommodity(UUID commodity) {
    Objects.requireNonNull(commodity);
    if(modifiedCommodities.containsKey(commodity))
      throw new IllegalStateException("commodity was already declared modified");
    if(newCommodities.contains(commodity))
      throw new IllegalStateException("commodity was already declared new");

    disappearingCommodities.add(commodity);
  }

  /**
   * Retrieves the set of commodities that were in the second cookbook but were
   * not present in the first. This represents a commodity that was newly added.
   *
   * @return a {@link Set} of {@link UUID} objects associated with new
   *         commodities
   */
  public Set<UUID> getNewCommodities() {
    return Collections.unmodifiableSet(newCommodities);
  }

  /**
   * Makes known a commodity that was in the second cookbook but was not present
   * in the first.
   *
   * @param commodity the unique {@link UUID} of the commodity that was added
   */
  public void addNewCommodity(UUID commodity) {
    Objects.requireNonNull(commodity);
    if(modifiedCommodities.containsKey(commodity))
      throw new IllegalStateException("commodity was already declared modified");
    if(disappearingCommodities.contains(commodity))
      throw new IllegalStateException("commodity was already declared disappearing");
  }
  
}
