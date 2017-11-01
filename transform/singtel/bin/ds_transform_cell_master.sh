#!/bin/bash

# Author: ragarwal
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_cell_master.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
CELLS_PATH=$BASEDIR/data/cells.txt
CELL_MAPPING_PATH=$BASEDIR/data/cell_mapping.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#CELLS_PATH=webhdfs://10.44.59.42:50070/data/cells/CELLID_201705*
#CELL_MAPPING_PATH=webhdfs://10.44.59.42:50070/data/cell_mapping/201705*
#OUTPUT_PATH=/user/dsuser/telco_planning/output_skylab/singtel/enriched_cell
#hadoop fs -rm -r -skipTrash hdfs://${OUTPUT_PATH}


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
-cells ${CELLS_PATH} \
-cell_mapping ${CELL_MAPPING_PATH} \
-fixed_cell_master ${OUTPUT_PATH} \
