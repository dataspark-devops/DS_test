#!/bin/bash
# Author: Ruby Agarwal
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

# Defaults
SPARK_SUBMIT_OPTS_FILE=$BASEDIR/conf/all-spark-submit-options.sh

print_help () {
  echo "Usage: index-core-toproistats.sh [options] master_bbc_info_path  fixed_cell_master_path collection_name zookeeper_address"
  echo ""
  echo "Options:"
  echo "-O    spark-submit options file (default: conf/all-spark-submit-options.sh)"
  exit ${1:-0}
}

# Parse command line arguments
while getopts ":O:h" opt; do
  case $opt in
    O)  SPARK_SUBMIT_OPTS_FILE="$OPTARG";;
    h)  print_help 0;;
    \?) print_help -1;;
  esac
done

# Positional parameters
shift $((OPTIND-1))
if [[ $# -ne 4 ]]; then
  print_help -1
fi

# Run
source $SPARK_SUBMIT_OPTS_FILE
spark-submit $SPARK_SUBMIT_OPTIONS \
--jars $BASEDIR/lib/transform-1.3.0-worker.jar \
--class com.dataspark.jobs.Runner \
$BASEDIR/lib/index-core-1.3.0-worker.jar \
$BASEDIR/conf/index-core-top_roi_stats.yaml \
--master_bbc_info "$1" \
--fixed_cell_master "$2" \
--output_top_roi_collection "$3" \
--zkHost "$4"

