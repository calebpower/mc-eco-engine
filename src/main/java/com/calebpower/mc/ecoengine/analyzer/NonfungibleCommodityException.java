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

import com.calebpower.mc.ecoengine.model.Commodity;

/**
 * An exception to be thrown when a {@link Commodity} is determined to have no
 * intrinsic value, having neither fungible ingredients nor any recipe that
 * provides fungibility e.g. it can't be bought or has no monetary value.
 *
 * @author Caleb L. Power <cpower@axonibyte.com>
 */
public class NonfungibleCommodityException extends Exception {

  private Commodity commodity = null;

  /**
   * Instantiates this exception.
   *
   * @param commodity the {@link Commodity} that was determined to be
   *        nonfungible
   */
  public NonfungibleCommodityException(Commodity commodity) {
    this.commodity = commodity;
  }

  /**
   * Retrieves the nonfungible commodity that caused this exception to be thrown.
   *
   * @return the nonfungible {@link Commodity}
   */
  public Commodity getCommodity() {
    return commodity;
  }
  
}
