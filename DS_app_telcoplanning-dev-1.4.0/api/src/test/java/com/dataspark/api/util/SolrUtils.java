/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.SolrInputDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by vishnuhr on 11/9/16. some solr utilities
 */
public class SolrUtils {

  /**
   * Parses an xml file containing documents exported from solr and creates Solr Input documents
   * Objects.
   *
   * @return List of Solr Input documents
   */
  public static List<SolrInputDocument> getSolrInputDocs(File xmlFile) throws Exception {

    ArrayList<SolrInputDocument> solrDocList = new ArrayList<SolrInputDocument>();

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(xmlFile);

    NodeList docList = doc.getElementsByTagName("doc");

    for (int docIdx = 0; docIdx < docList.getLength(); docIdx++) {

      Node docNode = docList.item(docIdx);

      if (docNode.getNodeType() == Node.ELEMENT_NODE) {

        SolrInputDocument solrInputDoc = new SolrInputDocument();

        Element docElement = (Element) docNode;

        NodeList fieldsList = docElement.getChildNodes();

        for (int fieldIdx = 0; fieldIdx < fieldsList.getLength(); fieldIdx++) {

          Node fieldNode = fieldsList.item(fieldIdx);

          if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {

            Element fieldElement = (Element) fieldNode;

            String fieldName = fieldElement.getAttribute("name");
            String fieldValue = fieldElement.getTextContent();
            if (fieldName.contains("_ss")) {
              String[] values = fieldValue.split("\n");
              for (String v : values) {
                if (!StringUtils.isEmpty(v)) {
                  solrInputDoc.addField(fieldName, v.trim());
                }
              }
            } else {
              solrInputDoc.addField(fieldName, fieldValue);
            }
          }

        }

        solrDocList.add(solrInputDoc);
      }
    }

    return solrDocList;
  }

}
