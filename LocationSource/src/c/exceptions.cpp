//
// Defines functions that throw exceptions into the Java code.
//

#include "exceptions.h"
#include <stdio.h>

/**
 * Throws a Java exception where name is the class name and msg
 * is the exception text.
 */
void throwException (JNIEnv *env, const char *name, const char *msg)
{
    // Lookup the exception class.
	jclass cls = env->FindClass( name );

    // Throw the exception (when we return to Java).
    if (cls != NULL)
    {
        env->ThrowNew( cls, msg );
	}

    // Free the class object.
    env->DeleteLocalRef( cls );
}

/**
 * Throws an IOException with msg as the text.
 */
void throwIOException (JNIEnv *env, const char *msg)
{
	throwException( env, "java/io/IOException", msg );
}

/**
 * Throws a LocationException with msg as the text.
 */
void throwLocationException (JNIEnv *env, const char *msg)
{
    throwException( env, "javax/microedition/location/LocationException", msg );
}

/**
 * Returns a human readible error message for the last system error
 * code.  The returned string does not need to be freed; it will be
 * reused on the next function call.
 */
WCHAR *getWinErrorMessage (DWORD last_error)
{
	static WCHAR errmsg[1024];
	if (!FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM,
		0,
		last_error,
		0,
		errmsg,
		511,
		NULL))
	{
		swprintf(errmsg, L"No error message for code %lu", last_error);
		return errmsg;
	}
	size_t last = wcslen(errmsg) - 1;
	while ((errmsg[last] == '\n') || (errmsg[last] == '\r')) 
    {
		errmsg[last] = 0;
		last --;
	}
	return errmsg;
}

/**
 * Throws an exception of type name for the last Windows error code.
 */
void throwLastError (JNIEnv *env, const char *name, const char *msg, DWORD last_error)
{
    if ( last_error == ERROR_SUCCESS )
    {
        throwException( env, name, msg );
    }
    else
    {
	    char errmsg[1064];
	    sprintf( errmsg, "%s; [%lu] %S", msg, last_error, getWinErrorMessage(last_error) );
	    throwException( env, name, errmsg );
    }
}

/**
 * Throws unhandled exception into Java.  Otherwise we'd crash the JVM.
 */
void throwUnhandledException (JNIEnv *env, const char *msg)
{
    throwLastError( env, "java/lang/RuntimeException", msg );
}

/**
 * Throws an IOException encapsulating the last Windows error code.
 */
void throwLastErrorAsIOException (JNIEnv *env, const char *msg, DWORD last_error)
{
	throwLastError( env, "java/io/IOException", msg, last_error );
}

/**
 * Throws a LocationException if the last operation resulted in a
 * Windows error code and return true.  Otherwise returns false.
 *
 * The calling function should return immediately if this returns true.
 * It does not halt execution of the function.
 */
bool throwLocationExceptionIfError (JNIEnv *env, const char *msg, DWORD last_error)
{
    if ( last_error != ERROR_SUCCESS )
    {
    	throwLastError( env, "javax/microedition/location/LocationException", msg, last_error );
        SetLastError( 0 );
        return true;
    }
    else
    {
        return false;
    }
}
