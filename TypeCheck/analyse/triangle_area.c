#include <stdio.h>
#include <jni.h>
#include "../test_shape_Circle.h"



JNIEXPORT jdouble JNICALL Java_test_shape_Triangle_compute_area_triangle(JNIEnv *env, jobject obj, jdouble b, jdouble h){
	
	double area = 1/2*b*h;
	return area;

}