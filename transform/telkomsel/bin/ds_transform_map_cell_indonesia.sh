#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_map_cell_indonesia.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
INPUT_PATH=$BASEDIR/data/fixed_cell_master_2016-06.txt
GEO_HIERARCHY_PATH=$BASEDIR/data/indonesia_hierarchy.dat
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#INPUT_PATH=file:///home/dsuser/TelcoPlanning/FIXED_CELL_MASTER_INPUT/fixed_cell_master_2016-06.txt
#GEO_HIERARCHY_PATH=file:///home/dsuser/TelcoPlanning/indonesia_hierarchy.dat
#OUTPUT_PATH=/user/dsuser/telco_planning/output/updated_fixed_cell
#hadoop fs -rm -r -skipTrash hdfs://${OUTPUT_PATH}

$SPARK_SUBMIT \
--master $SPARK_MASTER 
--num-executors 3 \
--executor-cores 1 \
--executor-memory 2g \
--driver-memory 2g \
--conf spark.akka.frameSize=128 \
--conf spark.rdd.compress=true \
--conf spark.scheduler.mode=FAIR \
--conf spark.core.connection.ack.wait.timeout=240 \
--conf spark.shuffle.manager=SORT \
--class com.dataspark.jobs.Runner $JAR $YAML \
-input_path ${INPUT_PATH} \
-geo_hierarchy_path ${GEO_HIERARCHY_PATH} \
-output_path ${OUTPUT_PATH}
