#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_aggregated_user_txn.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
FIXED_CELL_MASTER_PATH=$BASEDIR/data/fixed_cell_master_ut.txt
LBS_PLUS_PATH=$BASEDIR/data/lbs_plus_ut.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#FIXED_CELL_MASTER_PATH=file:///home/dsuser/TelcoPlanning/FIXED_CELL_MASTER_INPUT/ENRICHED/enriched_cell.txt
#LBS_PLUS_PATH=/user/dsuser/telco_planning/output/lbsplus/JABODETABEK/*/part*
#OUTPUT_PATH=/user/dsuser/telco_planning/output/enriched_txn
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
-lbs_plus ${LBS_PLUS_PATH} \
-fixed_cell_master ${FIXED_CELL_MASTER_PATH} \
-aggregated_user_txn ${OUTPUT_PATH}
