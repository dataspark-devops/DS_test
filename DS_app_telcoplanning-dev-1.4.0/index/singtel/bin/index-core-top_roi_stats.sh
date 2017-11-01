#!/bin/bash
# Author: Ruby Agarwal
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

# Defaults
SPARK_SUBMIT_OPTS_FILE=$BASEDIR/conf/all-spark-submit-options.sh

FIXED_CELL_MASTER=/Users/ragarwal/gitrepo/DS_app_telcoplanning/transform/singtel/data/fixed_cell_master.txt
TOP_ROI_NAME_ID_MAP=/Users/ragarwal/gitrepo/DS_app_telcoplanning/transform/singtel/data/top_roi_id_name_map.txt
OUTPUT_COLLECTION=singtel_top_roi_stats_201701
ZK_HOST=10.132.184.201:3181,10.132.184.202:3182,10.132.184.203:3183

# Run
source $SPARK_SUBMIT_OPTS_FILE
spark-submit $SPARK_SUBMIT_OPTIONS \
--jars $BASEDIR/lib/transform-1.3.0-worker.jar \
--class com.dataspark.jobs.Runner \
$BASEDIR/lib/index-core-1.3.0-worker.jar \
$BASEDIR/conf/index-core-top_roi_stats.yaml \
--fixed_cell_master ${FIXED_CELL_MASTER} \
--top_roi_name_id_map ${TOP_ROI_NAME_ID_MAP} \
--output_top_roi_collection ${OUTPUT_COLLECTION} \
--zkHost ${ZK_HOST} 