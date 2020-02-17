#!/bin/bash

export PATH=$PATH:/opt/soft/hadoop/bin

hdfs dfs -mkdir -p /user/dirac
hdfs dfs -ls /user/dirac
hdfs dfs -put #* /user/dirac
hdfs dfs -ls /user/dirac

