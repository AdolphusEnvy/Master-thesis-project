#include <jni.h>
#include <stdio.h>

JNIEXPORT void JNICALL
Java_HelloWorld_print(JNIEnv *env, jobject obj)
{
printf("Hello World!\n");
return;
}

//#include <jni.h>
//#include <iostream>
//using namespace std;
//JNIEXPORT void JNICALL
//Java_HelloWorld_print(JNIEnv *env, jobject obj)
//{
//cout<<"hello world"<<endl;
//return;
///}