//
// Defines functions that throw exceptions into the Java code.
//

#include <jni.h>
#include <windows.h>

/**
 * Throws a Java exception where name is the class name and msg
 * is the exception text.
 */
void throwException (JNIEnv *env, const char *name, const char *msg);

/**
 * Throws an IOException with msg as the text.
 */
void throwIOException (JNIEnv *env, const char *msg);

/**
 * Throws a LocationException with msg as the text.
 */
void throwLocationException (JNIEnv *env, const char *msg);

/**
 * Throws an exception of type name for the last Windows error code.
 */
void throwLastError (JNIEnv *env, const char *name, const char *msg, DWORD last_error = GetLastError());

/**
 * Throws unhandled exception into Java.  Otherwise we'd crash the JVM.
 */
void throwUnhandledException (JNIEnv *env, const char *msg);

/**
 * Throws an IOException encapsulating the last Windows error code.
 */
void throwLastErrorAsIOException (JNIEnv *env, const char *msg, DWORD last_error = GetLastError());

/**
 * Throws a LocationException if the last operation resulted in a
 * Windows error code.
 */
bool throwLocationExceptionIfError (JNIEnv *env, const char *msg, DWORD last_error = GetLastError());
