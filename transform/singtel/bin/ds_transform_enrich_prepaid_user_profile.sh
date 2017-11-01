#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_enrich_prepaid_user_profile.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
EDW_PREPAID_PATH=$BASEDIR/data/prepaid.csv
FIXED_CELL_MASTER_PATH=$BASEDIR/data/fixed_cell_master.txt
HOME_WORK_INFO_PATH=$BASEDIR/data/home_work_prepaid.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#EDW_PREPAID_PATH=webhdfs://10.44.59.42:50070/data/edw/prepaid/LA_MEDW_RPT_PR_ACTIVE_BASE_201704_Encrypted.csv
#FIXED_CELL_MASTER_PATH=file:///home/dsuser/TelcoPlanning/DS_app_telcoplanning/singtel/data/fixed_cell_master.txt
#HOME_WORK_INFO_PATH=webhdfs://10.44.59.42:50070/result/homework/monthly/201705/part-*
#OUTPUT_PATH=/user/dsuser/telco_planning/output_skylab/singtel/may/enriched_profile/prepaid
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
-edw_prepaid ${EDW_PREPAID_PATH} \
-fixed_cell_master ${FIXED_CELL_MASTER_PATH} \
-home_work_info ${HOME_WORK_INFO_PATH} \
-enriched_customer_profile ${OUTPUT_PATH}
