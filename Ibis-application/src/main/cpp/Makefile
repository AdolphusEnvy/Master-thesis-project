CXX=g++
CXXFLAGS=-O3 -Wall -g -fPIC
LDFLAGS=-Wl,--rpath,/usr/lib/x86_64-linux-gnu,--rpath,../lib
JAVAC=javac
JAVAH=javah
JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk-amd64
JAVAINC=-I${JAVA_HOME}/include -I${JAVA_HOME}/include/linux

default: build

OBJECTS=Driver.o

Driver.h:/Master-thesis-project/Ibis-application/build/classes/java/main/Driver.class
	$(JAVAH) -classpath ".:/lib/*:" Driver

libDriver.so:Driver.h
	#$(CXX) -shared $(CXXFLAGS) $(LDFLAGS) $(INCLUDES) $(LIBPATH) $(HDFSLIBPATH) -o $@  $(OBJECTS) $(MY_LIBS) $(LAPACK) $(CASA_LIBS) $(GLIBL) $(HDFSLIBS)
	$(CXX) -fPIC $(JAVAINC) -shared -o libDriver.so Driver.cpp

build: libDriver.so
