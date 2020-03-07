// Save as "HelloJNI.cpp"
#include <jni.h>       // JNI header provided by JDK
#include <iostream>    // C++ standard IO header
#include <string>
#include "Driver.h"  // Generated
#include "hdfs.h"
using namespace std;

// Implementation of the native method sayHello()
JNIEXPORT jint JNICALL Java_Driver_sayHello(JNIEnv *env, jobject thisObj,jstring inJNIStr) {
 const char *inCStr = env->GetStringUTFChars(inJNIStr, NULL);
    //if (NULL == inCStr) return NULL;
    ///const char *inCStr="123";
	cout << "Hello World from C++!"<<endl<< "The file is "<<inCStr<< endl;
   return 1;
}