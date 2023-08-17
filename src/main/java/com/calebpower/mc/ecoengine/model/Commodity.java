package com.calebpower.mc.ecoengine.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Commodity {

  private UUID id = null;
  private UUID workbook = null;
  private Set<UUID> recipes = new HashSet<>();
  
}
