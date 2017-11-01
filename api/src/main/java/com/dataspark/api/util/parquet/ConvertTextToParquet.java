/*
 * Copyright © DataSpark Pte Ltd 2014 - 2017. This software and any related documentation contain
 * confidential and proprietary information of DataSpark and its licensors (if any). Use of this
 * software and any related documentation is governed by the terms of your written agreement with
 * DataSpark. You may not use, download or install this software or any related documentation
 * without obtaining an appropriate licence agreement from DataSpark. All rights reserved.
 */
package com.dataspark.api.util.parquet;

import com.dataspark.api.data.TelcoPlanningConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Date;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang.StringUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

/**
 * @author ragarwal, 2017-08-14
 */
@Log4j
public class ConvertTextToParquet implements Serializable {
  private static final long serialVersionUID = -972007786057661146L;

  private StructType getSchema(String schemaPath) {
    StructType schema = new StructType();
    InputStream is = null;
    BufferedReader br = null;
    try {
      is = new FileInputStream(new File(schemaPath));
      br = new BufferedReader(new InputStreamReader(is));
      String line = "";
      if (is != null) {
        while ((line = br.readLine()) != null) {
          String[] tokens = line.split(TelcoPlanningConstants.Delimiters.COMMA);
          // name, dataType, isNullable
          schema = schema.add(String.valueOf(tokens[0].trim()), getDataType(tokens[1].trim()),
              Boolean.valueOf(tokens[2].trim()));
        }
      }
    } catch (FileNotFoundException e) {
      log.error("FileNotFoundException in convert text to parquet:", e);
    } catch (IOException e) {
      log.error("IOException in convert text to parquet:", e);
    } catch (Exception e) {
      log.error("Exception in convert text to parquet:", e);
    } finally {
      try {
        is.close();
        br.close();
      } catch (Throwable ignore) {
      }
    }
    return schema;
  }

  private DataType getDataType(String dataType) {
    switch (dataType) {
      case "string":
        return DataTypes.StringType;
      case "double":
        return DataTypes.DoubleType;
      case "float":
        return DataTypes.FloatType;
      case "int":
        return DataTypes.IntegerType;
      case "boolean":
        return DataTypes.BooleanType;
      case "long":
        return DataTypes.LongType;
      case "date":
        return DataTypes.DateType;
    }
    return DataTypes.StringType;
  }

  public void writeToParquet(String appName, String txtPath, String parquetPath, StructType schema,
      String delimiter) {
    log.info("Processing: " + appName);
    SparkConf conf = new SparkConf().setAppName(appName);
    JavaSparkContext sc = new JavaSparkContext(conf);
    SQLContext sqlContext = new SQLContext(sc);
    JavaRDD<String> rdds = sc.textFile(txtPath);

    JavaRDD<Row> rowRDD = rdds.map(new Function<String, Row>() {
      private static final long serialVersionUID = -8010486454519009290L;

      public Row call(String record) throws Exception {
        String[] recordValues = record.split(delimiter, -1);
        StructField[] structFields = schema.fields();
        Object[] rowFields = new Object[structFields.length];

        for (int i = 0; i < structFields.length; i++) {
          String fieldType = structFields[i].dataType().simpleString();
          String recordValue = recordValues[i];

          if ("string".equalsIgnoreCase(fieldType)) {
            rowFields[i] = String.valueOf(recordValue);
          } else if ("double".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Double.valueOf(recordValue));
          } else if ("float".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Float.valueOf(recordValue));
          } else if ("int".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Integer.valueOf(recordValue));
          } else if ("boolean".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Boolean.valueOf(recordValue));
          } else if ("long".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Long.valueOf(recordValue));
          } else if ("date".equalsIgnoreCase(fieldType)) {
            rowFields[i] = (StringUtils.isEmpty(recordValue) ? null : Date.valueOf(recordValue));
          }
        }

        return RowFactory.create(rowFields);
      }
    });

    Dataset<Row> celDataFrame = sqlContext.createDataFrame(rowRDD, schema);
    celDataFrame.write().parquet(parquetPath);
  }

  public static void main(String[] args) {
    ConvertTextToParquet converter = new ConvertTextToParquet();
    converter.writeToParquet(args[0], args[1], args[2], converter.getSchema(args[3]), args[4]);
  }
}
