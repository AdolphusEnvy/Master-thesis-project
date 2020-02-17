#!/bin/bash

hadoop classpath
javac -classpath $(/usr/bin/hadoop classpath) -d ./hdfs/ ./hdfs/HDFS_copy.java
/usr/lib/jvm/java-8-openjdk-amd64/bin/jar -cvf HDFS_copy.jar -C ./hdfs/ .

#hadoop jar ./HDFS_copy.jar HDFS_copy /tmp/sample_07.csv /tmp/output_mapred

hadoop jar ./HDFS_copy.jar HDFS_copy
