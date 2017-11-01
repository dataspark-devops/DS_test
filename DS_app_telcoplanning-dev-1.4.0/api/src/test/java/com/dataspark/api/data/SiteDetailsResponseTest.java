/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

/**
 * @author ragarwal, 2017-03-15
 */
public class SiteDetailsResponseTest {

  @Test
  public void testSiteDetailsResponseEquals() {
    Meta meta = new Meta();
    meta.setOrder(new String[] {"v1", "v2"});

    List<Object[]> results = new ArrayList<Object[]>();
    results.add(new Object[] {"v1", 10, 20f, 30d, 40l});

    Response resp1 = new Response(meta, results);
    Response resp2 = new Response(meta, results);
    Response resp3 = new Response();

    results.add(new Object[] {"v2", 10, 20f, 30d, 40l});
    resp3.setMeta(new Meta(new String[] {"v11", "v2"}));
    resp3.setResults(results);

    Set<Response> sites = new HashSet<Response>();
    sites.add(resp1);

    assertTrue(sites.contains(resp2));
    assertFalse(resp2.equals(resp3));
  }

  @Test
  public void testSiteDetailsResponseEqualsWithNulls() {
    Meta meta = new Meta();
    meta.setOrder(new String[] {"v1", "v2"});

    List<Object[]> results = new ArrayList<Object[]>();
    results.add(new Object[] {"v1", 10, 20f, 30d, 40l});

    Response resp1 = new Response(null, results);
    Response resp2 = new Response(null, results);

    Response resp3 = new Response(meta, null);
    Response resp4 = new Response(meta, null);

    assertTrue(resp1.equals(resp2));
    assertTrue(resp3.equals(resp4));
  }
}
