# Submitting Spark jobs (WIP)

# Usage
## 1 Accessing to nodes

You can start a shell session on the master node by running
```console
foo@bar:~$ docker exec -ti $(docker ps -a | grep spark-master | awk '{print $1}') /bin/bash
```
In order to access to hadoop node
```console
foo@bar:~$ docker exec -ti $(docker ps -a | grep hadoop | awk '{print $1}') /bin/bash
```



## Submitting a job

An example script to submit a job is given below.
```
```













### set environment
```console
export PATH=$PATH:/usr/lib/jvm/java-8-openjdk/bin
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
export HADOOP_CLASSPATH=$(/opt/soft/hadoop/bin/hadoop classpath)
```

### compile excon
```console
cd /opt/dirac/excon/JAVA
make -f Makefile.nocuda 
```

### Compile:
```console
javac -classpath ${HADOOP_CLASSPATH} FileSystemOperations.java
```

### Create JAR:
```console
jar -cvf WordCount.jar -C WordCount/ .
```

### Run:
```console
bin/hadoop jar WordCount.jar org.janzhou.wordcount /wordcount/input /wordcount/output
```





<!-- **/opt/dirac/scripts**  -->
