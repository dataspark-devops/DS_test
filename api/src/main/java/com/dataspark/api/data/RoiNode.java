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
import java.util.Map;
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
public class RoiNode implements Serializable {

  private static final long serialVersionUID = -7884831066860470271L;
  @Setter
  private String id;
  @Setter
  private String label;
  @Setter
  private String displayType;
  @Setter
  private String aggregatableAt;
  @Setter
  private String nodeValueIsA;

  @Setter
  private Map<String, Object> roiFilters; // roiLayer
  @Setter
  private List<RoiNode> children; // drop-down

  public RoiNode(String id, String label, String displayType, String aggregatableAt,
      String nodeValueIsA, Map<String, Object> roiFilters, List<RoiNode> children) {
    super();
    this.id = id;
    this.label = label;
    this.displayType = displayType;
    this.aggregatableAt = aggregatableAt;
    this.nodeValueIsA = nodeValueIsA;
    this.roiFilters = roiFilters;
    this.children = children;
  }

}
