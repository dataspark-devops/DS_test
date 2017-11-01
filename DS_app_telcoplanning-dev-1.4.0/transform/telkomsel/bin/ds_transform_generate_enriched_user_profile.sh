#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_generate_enriched_user_profile.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
CUSTOMER_PROFILE_PATH=$BASEDIR/data/customer_profile_ep.txt
DEVICE_INFO_PATH=$BASEDIR/data/device_info_ep.txt
FIXED_CELL_MASTER_PATH=$BASEDIR/data/fixed_cell_master_ep.txt
HOME_WORK_INFO_PATH=$BASEDIR/data/home_work_info_ep.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#CUSTOMER_PROFILE_PATH=/user/dsuser/telco_planning/profile/profile_data.txt
#DEVICE_INFO_PATH=file:///home/dsuser/TelcoPlanning/DEVICE_INFO_INPUT/ENRICHED/part-00000
#FIXED_CELL_MASTER_PATH=file:///home/dsuser/TelcoPlanning/FIXED_CELL_MASTER_INPUT/ENRICHED/enriched_cell.txt
#HOME_WORK_INFO_PATH=/user/dsuser/telco_planning/output/homework/part*
#OUTPUT_PATH=/user/dsuser/telco_planning/output/enriched_profile
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
-customer_profile ${CUSTOMER_PROFILE_PATH} \
-device_info ${DEVICE_INFO_PATH} \
-fixed_cell_master ${FIXED_CELL_MASTER_PATH} \
-home_work_info ${HOME_WORK_INFO_PATH} \
-enriched_customer_profile ${OUTPUT_PATH}
