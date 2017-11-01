#!/bin/sh

NAME=dataspark/api-telco-planning
DOCKER_VERSION=1.4.0
if [ -z "$DOCKER_REPO" ]; then
  echo Please set environment variables "DOCKER_REPO". e.g. dataspark-docker-snapshots.artifactory.datasparkanalytics.com
  exit 1
fi

HTTP_PORT=$1
if [ "$HTTP_PORT" == "" ];then
	HTTP_PORT=9005
fi

if [ "$JAVA_OPTS" == "" ];then
  JAVA_OPTS="-Xms512m -Xmx4g"
fi

cd $( dirname $0 )/..
BASE_DIR=$( pwd )
echo "NOTE: Ensure that /var/log/dataspark is owned by user who belongs to dataspark group"
LOG_DIR=/var/log/dataspark
#mkdir -p $LOG_DIR

echo "============================================================================="
echo "Starting api-telco-planning docker at port $HTTP_PORT :"
echo "You may override this port by providing port as first argument to the script."
echo "i.e sh api-run-docker-telcoplanning.sh  <port>"
echo "============================================================================="

echo "The logs emitted by the Docker Container will be available @ $LOG_DIR"
echo "Starting the docker...."
echo ""
docker run -d -e JAVA_OPTS="$JAVA_OPTS" -v $LOG_DIR:/var/log/dataspark \
  -v $BASE_DIR/conf:/opt/dataspark/api/conf \
  -p 0.0.0.0:$HTTP_PORT:8080 -it $DOCKER_REPO/$NAME:$DOCKER_VERSION
