#include <stdio.h>
#include <jni.h>
#include "../test_shape_Circle.h"



JNIEXPORT jdouble JNICALL Java_test_shape_Circle_compute_area_circle
(JNIEnv *env, 
	jobject obj, 
	jint radius){
	
	jclass thisClass = (*env)->GetObjectClass(env, obj);
	jfieldID piNumberFID = 
	(*env)->GetFieldID(env, 
		thisClass, "PI", "I");
	
	if (NULL == piNumberFID){ 
		return 0;
	}

	jint    piNumber = 
	(*env)->GetIntField(env, 
		obj, piNumberFID);

	return piNumber*(radius*radius);

}


function void miaFunziona(){
	return;
}