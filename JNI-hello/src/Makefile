CXX=g++
CXXFLAGS=-O3 -Wall -g -fPIC
LDFLAGS=-Wl,--rpath,/usr/lib/x86_64-linux-gnu,--rpath,../lib
JAVAC=javac
JAVAH=javah
# JAVA_HOME=/usr/lib/jvm/java-1.8.0*
JAVAINC=-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux -I/opt/soft/spark/jars -I/opt/soft/hadoop/include/
# HADOOP_CLASSPATH=$(shell /usr/bin/hadoop classpath)
HADOOP_SPARK_CLASSPATH=$(HADOOP_CLASSPATH):/opt/soft/spark/jars/*
default: build

OBJECTS=Driver.o
#JNI:HelloJNI.class
#	$(CXX) -fPIC $(JAVAINC) -shared -o libhello.so Driver.cpp
#class:HelloJNI.java
#	$(JAVAC) -classpath $(HADOOP_CLASSPATH) $<
Driver.class:Driver.java
	#echo $(HADOOP_SPARK_CLASSPATH)
	$(JAVAC) -classpath $(HADOOP_SPARK_CLASSPATH) $<
Driver.h:Driver.class
	$(JAVAH) $*
Driver.o:Driver.cpp Driver.h
	$(CXX) $(CXXFLAGS) $(INCLUDES) $(JAVAINC) $(HDFSINC) -c $<
#libDriver.so:$(OBJECTS) lib/liblargefft.a
libDriver.so:$(OBJECTS)
	#$(CXX) -shared $(CXXFLAGS) $(LDFLAGS) $(INCLUDES) $(LIBPATH) $(HDFSLIBPATH) -o $@  $(OBJECTS) $(MY_LIBS) $(LAPACK) $(CASA_LIBS) $(GLIBL) $(HDFSLIBS)
	g++ -fPIC $(JAVAINC) -shared -o libDriver.so Driver.cpp
jar:Driver.class
	@echo "Manifest-Version: 1.0" > manifest.txt
	@echo "Main-Class: Driver" >> manifest.txt
	@echo "Class-Path: ." >> manifest.txt
	# @echo "Class-Path: /home/ubuntu/sagecal-on-spark/excon/JAVA" >> manifest.txt
	# @echo "Library-Path: /home/ubuntu/sagecal-on-spark/excon/JAVA" >> manifest.txt
	# @echo "Library-Path: /usr/local/lib/" >> manifest.txt
	@echo "" >> manifest.txt
	@jar cmfv manifest.txt Driver.jar $*
	@rm -f manifest.txt
build:libDriver.so Driver.class Driver.h Driver.o jar
