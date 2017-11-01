package com.dataspark.api.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ashutoshprasar
 *
 *         Written on 2 Mar 2017
 */
@Getter
@ToString
public class CellDetailsRequest implements Serializable {

  private static final long serialVersionUID = -4129918857295377195L;
  // key = ROI layer, value = list of corresponding ROI IDs
  @Setter
  private Map<String, List<String>> roiFilters;
  @Setter
  private String periodType;
  @Setter
  private List<Period> periodRange;
  @Setter
  private List<String> indicators;
  @Setter
  private Map<String, List<String>> filterUser;
  @Setter
  private Map<String, List<String>> filterDevice;
  @Setter
  private Map<String, List<String>> filterCell;
  @Setter
  private List<String> sites;
}
