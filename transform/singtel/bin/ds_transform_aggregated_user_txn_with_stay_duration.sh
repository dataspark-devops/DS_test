#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_aggregated_user_txn_with_stay_duration.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
FIXED_CELL_MASTER_PATH=$BASEDIR/data/fixed_cell_master.txt
STAY_DURATION_DATA=$BASEDIR/data/stay_duration_per_imsi_per_cell.txt
LBS_PLUS_PATH=$BASEDIR/data/lbs_plus.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#FIXED_CELL_MASTER_PATH=file:///home/dsuser/TelcoPlanning/DS_app_telcoplanning/singtel/data/fixed_cell_master.txt
#STAY_DURATION_DATA=/user/dsuser/telco_planning/output_skylab/singtel/stayduration/*
#LBS_PLUS_PATH=webhdfs://10.44.59.42:50070/data/lbs_ndc/raw_201705*
#OUTPUT_PATH=/user/dsuser/telco_planning/output_skylab/singtel/may/aggUsrTxn
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
