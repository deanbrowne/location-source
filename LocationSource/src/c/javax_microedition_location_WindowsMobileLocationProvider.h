/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class javax_microedition_location_WindowsMobileLocationProvider */

#ifndef _Included_javax_microedition_location_WindowsMobileLocationProvider
#define _Included_javax_microedition_location_WindowsMobileLocationProvider
#ifdef __cplusplus
extern "C" {
#endif
#undef javax_microedition_location_WindowsMobileLocationProvider_AVAILABLE
#define javax_microedition_location_WindowsMobileLocationProvider_AVAILABLE 1L
#undef javax_microedition_location_WindowsMobileLocationProvider_TEMPORARILY_UNAVAILABLE
#define javax_microedition_location_WindowsMobileLocationProvider_TEMPORARILY_UNAVAILABLE 2L
#undef javax_microedition_location_WindowsMobileLocationProvider_OUT_OF_SERVICE
#define javax_microedition_location_WindowsMobileLocationProvider_OUT_OF_SERVICE 3L
/* Inaccessible static: instance */
/* Inaccessible static: instance */
/*
 * Class:     javax_microedition_location_WindowsMobileLocationProvider
 * Method:    startGPS
 * Signature: ()Z
 */
JNIEXPORT jboolean JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_startGPS
  (JNIEnv *, jobject);

/*
 * Class:     javax_microedition_location_WindowsMobileLocationProvider
 * Method:    stopGPS
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_stopGPS
  (JNIEnv *, jobject);

/*
 * Class:     javax_microedition_location_WindowsMobileLocationProvider
 * Method:    getGPSEvent
 * Signature: (III)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_getGPSEvent
  (JNIEnv *, jobject, jint, jint, jint);

#ifdef __cplusplus
}
#endif
#endif
