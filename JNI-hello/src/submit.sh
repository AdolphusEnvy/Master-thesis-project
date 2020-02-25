#!/bin/bash

#
#export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
#export JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH:/opt/soft/hadoop/lib/native:/opt/dirac/JNI_hello/src
#export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/soft/hadoop/lib/native:/opt/dirac/JNI_hello/src
#export SPARK_YARN_USER_ENV="JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH,LD_LIBRARY_PATH=$LD_LIBRARY_PATH"

#/opt/soft/spark/bin/spark-submit \
#        --master spark://spark-master:6066 \
#        --deploy-mode cluster  \
#        --executor-cores 1 \
#        --driver-class-path /opt/dirac/JNI_hello/src \
#        --driver-library-path JAVA_LIBRARY_PATH=$JAVA_LIBRARY_PATH:/opt/soft/hadoop/lib/native:/opt/dirac/JNI_hello/src \
#        --class Driver \
#        /opt/dirac/JNI_hello/src/Driver.jar -123
#/opt/soft/spark/bin/spark-submit \
#        --driver-class-path /opt/dirac/JNI_hello/src \
#        --class Driver \
#        --master spark://spark-master:6066 \
#        --deploy-mode cluster  \
#        --executor-cores 1 \
#        --driver-library-path /usr/local/lib \
#        Driver.jar -m

/opt/soft/spark/bin/spark-submit \
        --master spark://spark-master:6066 \
        --deploy-mode cluster  \
        --executor-cores 1 \
        --driver-class-path /opt/dirac/JNI_hello/src \
        --driver-library-path /opt/dirac/JNI_hello/src \
        --class Driver \
        /opt/dirac/JNI_hello/src/Driver.jar 123