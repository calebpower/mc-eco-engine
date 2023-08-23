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
 * Describes an analysis of the commodities within a cookbook.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class Analysis {

  private Map<UUID, Float> fungibleCommodities = new HashMap<>();
  private Set<UUID> nonfungibleCommodities = new HashSet<>();

  /**
   * Retrieves a map of fungible commodities and their respective values.
   *
   * @return a {@link Map} of unique {@link UUID} objects associated with
   *         various commodities, with {@link Float} values representing
   *         their respective aggregated production costs
   */
  public Map<UUID, Float> getFungibleCommodities() {
    return Collections.unmodifiableMap(fungibleCommodities);
  }

  /**
   * Adds a fungible commodity to this analysis report.
   *
   * @param commodity the unique {@link UUID} of the commodity in question
   * @param value the aggregated costs to produce that particular commodity
   */
  public void addFungibleCommodity(UUID commodity, float value) {
    Objects.requireNonNull(commodity);
    if(nonfungibleCommodities.contains(commodity))
      throw new IllegalStateException("commodity was already declared nonfungible");

    fungibleCommodities.put(commodity, value);
  }

  /**
   * Determines whether or not a commodity is in the map of fungible
   * commodities. This method will return {@code false} if the commodity isn't
   * in the list of fungible commodities, even if it's not explicitly decalred
   * as a nonfungible commodity.
   *
   * @return {@code true} iff the commodity is fungible
   */
  public boolean isFungible(UUID commodity) {
    return fungibleCommodities.containsKey(commodity);
  }

  /**
   * Retrieves the set of all nonfungible commodities.
   *
   * @return a {@link Set} of all {@link UUID} objects associated with
   *         nonfungible commodities
   */
  public Set<UUID> getNonfungibleCommodities() {
    return Collections.unmodifiableSet(nonfungibleCommodities);
  }

  /**
   * Adds a nonfungible commodity to this analysis report.
   *
   * @param commodity the unique {@link UUID} of the nonfungible commodity
   */
  public void addNonfungibleCommodity(UUID commodity) {
    Objects.requireNonNull(commodity);
    if(fungibleCommodities.containsKey(commodity))
      throw new IllegalStateException("commodity was already declared fungible");

    nonfungibleCommodities.add(commodity);
  }
  
}
