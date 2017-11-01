#!/bin/bash

# Author: Ashutosh Prasar
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_cdr_to_lbs.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
INPUT_PATH=$BASEDIR/data/cdr_transactions_sample.txt
CELL_MASTER=$BASEDIR/data/fixed_cell_master_sample.txt
CUSTOMER_PROFILE=$BASEDIR/data/cust_profile_sample.csv
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#INPUT_PATH=/user/dsuser/telco_planning/cdr/cdr_data*
#OUTPUT_PATH=/user/dsuser/telco_planning/output/lbsplus
#CELL_MASTER=file://$BASEDIR/../FIXED_CELL_MASTER_INPUT/fixed_cell_master_2016-06.txt
#CUSTOMER_PROFILE=/user/dsuser/telco_planning/profile/profile_data.txt
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
-input ${INPUT_PATH} \
-output ${OUTPUT_PATH} \
-cell_master ${CELL_MASTER} \
-customer_profile ${CUSTOMER_PROFILE}
