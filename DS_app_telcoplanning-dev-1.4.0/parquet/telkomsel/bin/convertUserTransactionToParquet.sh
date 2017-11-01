export HADOOP_CONF_DIR=~/installs/hadoop-2.7.3/etc/hadoop

SPARK_MASTER=local

APP_NAME=ConvertUserTxnToParquet
TEXT_PATH=../data/userTxn.txt
PARQUET_PATH=../results/userTxn
SCHEMA_PATH=../conf/userTxn.txt
DELIMITER="\|"

rm -rf ${PARQUET_PATH}
#hadoop fs -rm -r -skipTrash hdfs://${PARQUET_PATH}

~/installs/spark-2.0.0-bin-hadoop2.7/bin/spark-submit  \
--master ${SPARK_MASTER} \
--verbose \
--conf spark.akka.frameSize=128 \
--conf spark.rdd.compress=true \
--conf spark.core.connection.ack.wait.timeout=240 \
--conf spark.shuffle.manager=SORT \
--driver-memory 10G \
--executor-memory 10G \
--num-executors 12 \
--executor-cores 4 \
--class com.dataspark.api.util.parquet.ConvertTextToParquet ../lib/api-telcoplanning-1.4.0-SNAPSHOT.jar ${APP_NAME} ${TEXT_PATH} ${PARQUET_PATH} ${SCHEMA_PATH} ${DELIMITER}