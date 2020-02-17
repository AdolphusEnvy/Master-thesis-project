# Introduction

This code adds HDFS support to the SageCal code. The Docker images of SageCal use this code in order to run calibration jobs. 

For the issues related to SageCal itself, please create a ticket at [https://github.com/nlesc-dirac/sagecal](https://github.com/nlesc-dirac/sagecal).

## Setup groups for permissions

```bash
groupadd dirac
usermod -a -G dirac ubuntu
```

## Setup the data folder

As root user:

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

```

## Prepare the data folder

hadoop fs -mkdir /data
hdfs dfs -chgrp dirac /data
#hdfs dfs -chown –R ubuntu:dirac
hdfs dfs -ls /
hdfs dfs -ls hdfs://node1:9000/user/ubuntu
hdfs dfs -chmod –R 755 /data


## Add a new dataset

hdfs dfs -ls hdfs://node1:9000/user/ubuntu
hdfs dfs -put sm.ms /user/ubuntu




sc.textFile(hdfs://NamenodeIPAddress:Port/DirectoryLocation)
example: sc.textFile(hdfs://127.0.0.1:8020/user/movies)
