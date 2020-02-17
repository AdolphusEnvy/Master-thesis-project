#!/bin/sh

ids=$(hadoop fs -stat "%n" /host/datasets/sm$1*)

for fname in $ids
do 
    echo $fname

    /spark/bin/spark-submit \
        --driver-class-path /host/excon_src_v2/JAVA \
        --class Driver \
        --master spark://spark-master:6066 \
        --deploy-mode cluster  \
        --executor-cores 1 \
        --driver-library-path /usr/local/lib \
        Driver.jar -m  /host/datasets/$fname
done






