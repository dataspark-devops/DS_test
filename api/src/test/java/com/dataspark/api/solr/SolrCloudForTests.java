/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
*/
package com.dataspark.api.solr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.JettyConfig;
import org.apache.solr.cloud.MiniSolrCloudCluster;
import org.apache.solr.cloud.ZkTestServer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;


/**
 * Created by vishnuhr on 11/9/16. For testing purposes.
 */
@TestConfiguration
@EnableAutoConfiguration
@Log4j
public class SolrCloudForTests {

  public static final int ZK_PORT = 2188;

  private static String SOLR_XML =
      "<solr>\n\n  <str name=\"shareSchema\">${shareSchema:false}</str>\n  <str name=\"configSetBaseDir\">${configSetBaseDir:configsets}</str>\n  <str name=\"coreRootDirectory\">${coreRootDirectory:.}</str>\n\n  <shardHandlerFactory name=\"shardHandlerFactory\" class=\"HttpShardHandlerFactory\">\n    <str name=\"urlScheme\">${urlScheme:}</str>\n    <int name=\"socketTimeout\">${socketTimeout:90000}</int>\n    <int name=\"connTimeout\">${connTimeout:15000}</int>\n  </shardHandlerFactory>\n\n  <solrcloud>\n    <str name=\"host\">127.0.0.1</str>\n    <int name=\"hostPort\">${hostPort:8983}</int>\n    <str name=\"hostContext\">${hostContext:solr}</str>\n    <int name=\"zkClientTimeout\">${solr.zkclienttimeout:30000}</int>\n    <bool name=\"genericCoreNodeNames\">${genericCoreNodeNames:true}</bool>\n    <int name=\"leaderVoteWait\">10000</int>\n    <int name=\"distribUpdateConnTimeout\">${distribUpdateConnTimeout:45000}</int>\n    <int name=\"distribUpdateSoTimeout\">${distribUpdateSoTimeout:340000}</int>\n  </solrcloud>\n  \n</solr>\n";
  private static int numServersInCloud = 1;

  @Bean(destroyMethod = "selfClean")
  @Qualifier("zkDir")
  public DirWrapper zkDir() {
    return new DirWrapper(new File("ZK_TEST_HOME"));
  }

  @Bean(destroyMethod = "selfClean")
  @Qualifier("solrHomeDir")
  public DirWrapper solrHomeDir() {
    return new DirWrapper(new File("SOLR_TEST_HOME"));
  }

  @Bean(destroyMethod = "shutdown")
  public ZkTestServer createZk(@Qualifier("zkDir") DirWrapper zkDir)
      throws IOException, InterruptedException {
    FileUtils.deleteDirectory(zkDir.getFile());
    if (!zkDir.getFile().mkdir()) {
      throw new IOException(
          "Failed to create test zookeper dir. " + zkDir.getFile().getAbsolutePath());
    }
    ZkTestServer zkServer = new ZkTestServer(zkDir.getFile().getAbsolutePath(), ZK_PORT);
    zkServer.run();
    return zkServer;
  }

  @Bean(destroyMethod = "shutdown")
  public MiniSolrCloudCluster createSolrCluster(@Qualifier("solrHomeDir") DirWrapper solrHomeDir,
      ZkTestServer zkTestServer) throws Exception {
    FileUtils.deleteDirectory(solrHomeDir.getFile());
    if (!solrHomeDir.getFile().mkdir()) {
      throw new IOException("Failed to create directory for test solr cluster. "
          + solrHomeDir.getFile().getAbsolutePath());
    }
    MiniSolrCloudCluster miniSolrCloudCluster = new MiniSolrCloudCluster(numServersInCloud,
        FileSystems.getDefault().getPath(solrHomeDir.getFile().getAbsolutePath()), SOLR_XML,
        createJettyConfig(), zkTestServer);
    log.info("Started Mini Solr Cloud Cluster for tests. ZkServer Address: "
        + zkTestServer.getZkAddress());
    return miniSolrCloudCluster;
  }

  private JettyConfig createJettyConfig() {
    return JettyConfig.builder().setPort(8989) // if you want multiple servers in the solr cloud
                                               // comment it out.
        .setContext("/solr").stopAtShutdown(true).withServlets(new HashMap<ServletHolder, String>())
        .withSSLConfig(null).build();
  }

  /**
   * Simple Wrapper over a Directory.
   */
  @Getter
  @AllArgsConstructor
  public class DirWrapper {
    private final File file;

    public void selfClean() {
      if (file != null) {
        try {
          FileUtils.deleteDirectory(file);
        } catch (IOException e) {
          log.warn("Failed to clean up File file : " + file.getAbsolutePath(), e);
        }
      }
    }
  }
}
