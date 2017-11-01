#!/bin/bash

# Author: Ashutosh Prasar
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

SPARK_SUBMIT=spark-submit
SPARK_MASTER=yarn
JAR=$BASEDIR/lib/transform-1.3.0-worker.jar
YAML=$BASEDIR/conf/ds_transform_device_information.yaml

#--------------------------------------------------------------------------------------------------
# ----The default paths points to local file system. Comment this section if reading from HDFS ----
#--------------------------------------------------------------------------------------------------
INPUT_PATH=file://$BASEDIR/data/device_info_sample.txt
FREQ_MAPPING_FILE_PATH=file://$BASEDIR/data/freq_band_mapping.txt
OUTPUT_PATH=$BASEDIR/result
rm -r ${OUTPUT_PATH}

#--------------------------------------------------------------------------------------------------
# -------------- Remove comments and update to correct path when reading from HDFS ----------------
#--------------------------------------------------------------------------------------------------
#INPUT_PATH=/user/dsuser/telco_planning/device_data/imei_reff_add_201606.txt
#FREQ_MAPPING_FILE_PATH=file://$BASEDIR/data/freq_band_mapping.txt
#OUTPUT_PATH=/user/dsuser/telco_planning/output/enriched_device
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
-input_path ${INPUT_PATH} \
-output_path ${OUTPUT_PATH} \
-freq_band_mapping_path ${FREQ_MAPPING_FILE_PATH}
