#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_aggregated_user_txn_with_stay_duration.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
FIXED_CELL_MASTER_PATH=$BASEDIR/data/fixed_cell_master_sd.txt
STAY_DURATION_DATA=$BASEDIR/data/stay_duration_sd.txt
LBS_PLUS_PATH=$BASEDIR/data/lbs_plus_sd.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#FIXED_CELL_MASTER_PATH=.file:///home/dsuser/TelcoPlanning/FIXED_CELL_MASTER_INPUT/ENRICHED/enriched_cell.txt
#STAY_DURATION_DATA=/user/dsuser/telco_planning/output/stayduration/bandung/*
#LBS_PLUS_PATH=/user/dsuser/telco_planning/output/lbsplus/stay/BANDUNG/*
#OUTPUT_PATH=/user/dsuser/telco_planning/output/aggUsrTxnSD/bandung
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
-lbs_plus ${LBS_PLUS_PATH} \
-fixed_cell_master ${FIXED_CELL_MASTER_PATH} \
-stay_duration_data ${STAY_DURATION_DATA} \
-aggregated_user_txn ${OUTPUT_PATH}
