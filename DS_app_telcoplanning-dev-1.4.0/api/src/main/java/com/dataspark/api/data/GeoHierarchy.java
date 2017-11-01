/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import java.io.Serializable;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ragarwal, 2017-05-03
 */
@Getter
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class GeoHierarchy implements Serializable {

  private static final long serialVersionUID = 4487379324606772642L;
  @Setter
  private GeoNodeType geoNodeType;
  @Setter
  private GeoNodeDepth geoNodeDepth; // geojson file bbc, kab, kech etc.
  @Setter
  private GeoDisplayType displayType;
  @Setter
  private GeoNodeDepth aggregatableAt;
  @Setter
  private List<GeoHierarchy> children;

  public GeoHierarchy(GeoNodeType geoNodeType, GeoNodeDepth geoNodeDepth,
      GeoDisplayType displayType, GeoNodeDepth aggregatableAt, List<GeoHierarchy> children) {
    super();
    this.geoNodeType = geoNodeType;
    this.geoNodeDepth = geoNodeDepth;
    this.displayType = displayType;
    this.aggregatableAt = aggregatableAt;
    this.children = children;
  }

}
