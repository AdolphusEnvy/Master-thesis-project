#!/bin/bash


export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH:/usr/lib/hadoop/lib/native:/home/ubuntu/sagecal-on-spark/excon/JAVA
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/hadoop/lib/native:/home/ubuntu/sagecal-on-spark/excon/JAVA
export SPARK_YARN_USER_ENV="JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH,LD_LIBRARY_PATH=$LD_LIBRARY_PATH"


/usr/bin/spark-submit \
    --driver-class-path /home/ubuntu/sagecal-on-spark/excon/JAVA \
    --class Driver \
    --master spark://node1:7077 \
    --deploy-mode cluster  \
    --executor-cores 1 \
    --driver-library-path /usr/local/lib \
    --files hdfs://node1:9000/user/ubuntu/sm.ms \
    --verbose \
    Driver.jar -m /home/ubuntu/sm.ms

    # --files hdfs://node1:9000/user/ubuntu/sm.ms \

    # -m hdfs hdfs://node1:9000/user/ubuntu/sm.ms
    # -m /home/ubuntu/sm.ms
    # -m  /host/datasets/$fname
