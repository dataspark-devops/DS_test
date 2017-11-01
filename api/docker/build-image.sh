#!/bin/sh
set -e

NAME=dataspark/api-telco-planning
if [ -z "$DOCKER_REPO" ]; then
  echo Please set environment variables "DOCKER_REPO". e.g. dataspark-docker-snapshots.artifactory.datasparkanalytics.com
  exit 1
fi

docker_dir=$( cd $( dirname ${BASH_SOURCE[0]} ); pwd)
cd $docker_dir

function cleanup {
cd $docker_dir
  rm -rf ./conf
  rm -rf ./lib
  rm -rf ./bin
}
trap cleanup EXIT

IMAGE_VERSION=`cat ../../pom.xml | grep version -m 1 | awk -F'[><]' '{print $3}'`
echo $IMAGE_VERSION
echo "Will use artifact version $IMAGE_VERSION in tagging docker image in $DOCKER_REPO..."

cp -R ../../target/app-*/app-*/* .

#you should not have a docker run script inside the docker :)
rm -rf ./bin/api-run-docker-telcoplanning.sh
rm -rf README.md

#Build
docker build -f api.Dockerfile -t $NAME:$IMAGE_VERSION \
  -t $DOCKER_REPO/$NAME:$IMAGE_VERSION --label VERSION=$NAME:$IMAGE_VERSION .
