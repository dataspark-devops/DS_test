#!/bin/bash
BASEDIR=$( cd $( dirname ${BASH_SOURCE[0]} )/..; pwd)

# Defaults
SPARK_SUBMIT_OPTS_FILE=$BASEDIR/conf/all-spark-submit-options.sh

print_help () {
  echo "Usage: index-core-usertxn.sh [options] enriched_user_profile_path  aggregated_user_txn_path collection_name zookeeper_address"
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
$BASEDIR/conf/index-core-user_txn.yaml \
--enriched_user_profile "$1" \
--aggregated_user_txn "$2" \
--output_user_txn_collection "$3" \
--zkHost "$4" \
--multivalue_field_delimiter ","

