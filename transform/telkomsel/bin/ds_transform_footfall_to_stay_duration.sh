#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_footfall_to_stay_duration.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
FOOTFALL_PATH=../data/footfall_per_imsi_per_cell.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#FOOTFALL_PATH=/user/dsuser/telco_planning/output/visitcell/bandung/*
#OUTPUT_PATH=/user/dsuser/telco_planning/output/stayduration/bandung
#hadoop fs -rm -r -skipTrash hdfs://${OUTPUT_PATH}

$SPARK_SUBMIT \
--conf spark.akka.frameSize=128 \
--conf spark.rdd.compress=true \
--conf spark.scheduler.mode=FAIR \
--conf spark.core.connection.ack.wait.timeout=240 \
--conf spark.shuffle.manager=SORT \
--driver-memory 10G \
--executor-memory 10G \
--num-executors 12 \
--executor-cores 4 \
--master $SPARK_MASTER \
--class com.dataspark.jobs.Runner $JAR $YAML \
-footfall_per_imsi_per_cell ${FOOTFALL_PATH} \
-stay_duration_per_imsi_per_cell ${OUTPUT_PATH}
