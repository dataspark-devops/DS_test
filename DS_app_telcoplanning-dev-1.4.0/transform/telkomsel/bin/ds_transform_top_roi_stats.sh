#!/bin/bash

BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_top_roi_stats.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
MASTER_BBC=$BASEDIR/data/master_bbc_info.txt
CELL_MASTER=$BASEDIR/data/fixed_cell_master_sample.txt
TOP_ROI_STATS=$BASEDIR/result
rm -rf ${TOP_ROI_STATS}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#MASTER_BBC=/user/dsuser/telco_planning/master_bbc/master_bbc_map_info_201606.csv
#CELL_MASTER=/user/dsuser/telco_planning/cell/fixed_cell_master_2016-06.txt
#TOP_ROI_STATS=/user/dsuser/telco_planning/output/topROIStats
#hadoop fs -rm -r -skipTrash hdfs://${TOP_ROI_STATS}

$SPARK_SUBMIT \
--conf spark.akka.frameSize=128 \
--conf spark.rdd.compress=true \
--conf spark.core.connection.ack.wait.timeout=240 \
--conf spark.shuffle.manager=SORT \
--driver-memory 10G \
--executor-memory 10G \
--num-executors 12 \
--executor-cores 4 \
--master $SPARK_MASTER \
--class com.dataspark.jobs.Runner $JAR $YAML \
-master_bbc_info ${MASTER_BBC} \
-fixed_cell_master ${CELL_MASTER} \
-output_path ${TOP_ROI_STATS}