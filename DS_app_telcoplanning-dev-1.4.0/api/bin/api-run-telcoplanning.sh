#!/bin/bash
cd $( dirname $0 )/..
BASE_DIR=$( pwd )
AUDIT_DIR=/var/log/dataspark/api-telco-planning
mkdir -p $AUDIT_DIR
CACHE_DIR=/tmp

# Defaults
PROP_FILE=$BASE_DIR/conf/api-telcoplanning.properties

if [ "$JAVA_OPTS" == "" ];then
  JAVA_OPTS="-Xms512m -Xmx4g"
fi

print_help () {
  echo "Usage: api-run-telcoplanning.sh [options]"
  echo ""
  echo "Options:"
  echo "-p    Properties file (default: BASEDIR/conf/api-telcoplanning.properties)"
  exit ${1:-0}
}

# Parse command line arguments
while getopts ":p:h" opt; do
  case $opt in
    p)  PROP_FILE=$OPTARG;;
    h)  print_help 0;;
    \?) print_help -1;;
  esac
done

# Positional parameters
shift $((OPTIND-1))
if [[ $# -ne 0 ]]; then
  print_help -1
fi

java \
-cp ${BASE_DIR}/conf \
-Daudit_log_dir=${AUDIT_DIR} \
-Djava.io.tmpdir=${CACHE_DIR} \
-Dlog_dir=$AUDIT_DIR \
$JAVA_OPTS \
-jar ${BASE_DIR}/lib/api-telcoplanning-1.4.0*exec.jar \
--logging.config=file:${BASE_DIR}/conf/api-telcoplanning-logback.xml \
--spring.config.location=file:${PROP_FILE}
