//
// JNI function definitions.
//

#include "javax_microedition_location_WindowsMobileLocationProvider.h"
#include "exceptions.h"
#include <Gpsapi.h>
#include <Service.h>

// Redefine Java's constants for GPS state (from LocationProvider).
#define AVAILABLE                javax_microedition_location_WindowsMobileLocationProvider_AVAILABLE
#define TEMPORARILY_UNAVAILABLE  javax_microedition_location_WindowsMobileLocationProvider_TEMPORARILY_UNAVAILABLE
#define OUT_OF_SERVICE           javax_microedition_location_WindowsMobileLocationProvider_OUT_OF_SERVICE


//////////////////////////////////////////////////////////////////////////
// Globals
//////////////////////////////////////////////////////////////////////////

// Handle to the GPS Intermediate Driver.
HANDLE hGPSDevice;

// Event handle triggered when a new location arrives.
HANDLE hNewLocationData;

// Event handle triggered when the GPS changes state.
HANDLE hDeviceStateChange;


//////////////////////////////////////////////////////////////////////////
// Helper Methods
//////////////////////////////////////////////////////////////////////////

/**
 * Checks if any GPS receivers are configured on this device.
 *
 * A description of the GPS Intermediate Driver registry settings is online at:
 *   http://msdn2.microsoft.com/en-us/library/ms889972.aspx
 *
 * For reference, the Samsung BlackJack II comes with built-in GPS and these
 * registry settings:
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver]
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer]
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer\ActiveDevice]
 *     "Dll"="PHONEGPSID.dll"
 *     "Context"=dword:12344160
 *     "Flags"=dword:00000002
 *     "Keep"=dword:00000001
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Drivers]
 *     "CurrentDriver"="SamsungGPSHardware"
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Drivers\SamsungGPSHardware]
 *     "FriendlyName"="Samsung GPS Hardware, ver 0.1"
 *     "InterfaceType"="PHONE"
 *
 * The Samsung BlackJack (I) comes without built-inGPS and these registry settings:
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver]
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer]
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer\ActiveDevice]
 *     "Dll"="GPSID.dll"
 *     "Context"=dword:12344160
 *     "Flags"=dword:00000002
 *     "Keep"=dword:00000001
 *   [HKEY_LOCAL_MACHINE\System\CurrentControlSet\GPS Intermediate Driver\Drivers]
 */
BOOL hasGPS ()
{
    HKEY hkey;
    TCHAR szDriver[256];
    DWORD dwSize;

    if ( RegOpenKeyEx(HKEY_LOCAL_MACHINE, _T("System\\CurrentControlSet\\GPS Intermediate Driver\\Drivers"), 0, 0, &hkey) == ERROR_SUCCESS )
    {
        DWORD dwType = REG_SZ;
        dwSize = sizeof( szDriver );

        LONG result = RegQueryValueEx( hkey, _T("CurrentDriver"), NULL, &dwType, (PBYTE)&szDriver, &dwSize );
        RegCloseKey(hkey);

        if ( (result != ERROR_SUCCESS) || (dwSize == 0) )
        {
            // No GPS receiver is registered.
            return FALSE;
        }
        else
        {
            // A GPS receiver is available.
            return TRUE;
        }
    }
    else  // No registry key
    {
        return FALSE;
    }
}

/**
 * Converts a GPS Intermediate Driver state integer to a Java JSR-179 state integer.
 */
jint convertState (GPS_DEVICE &state)
{
    // Translate the state to the JSR-179 codes.
    switch (state.dwServiceState)
    {
    case SERVICE_STATE_ON:  // The service is turned on.
        return AVAILABLE;

    case SERVICE_STATE_OFF:            // The service is turned off.
    case SERVICE_STATE_STARTING_UP:    // The service is in the process of starting up.
    case SERVICE_STATE_SHUTTING_DOWN:  // The service is in the process of shutting down.
    case SERVICE_STATE_UNLOADING:      // The service is in the process of unloading.
    case SERVICE_STATE_UNINITIALIZED:  // The service is not uninitialized.
    case SERVICE_STATE_UNKNOWN:        // The state of the service is unknown.
        return TEMPORARILY_UNAVAILABLE;
    }

    // Default.
    return TEMPORARILY_UNAVAILABLE;
}

/**
 * Converts a jint to a Java Integer object.
 */
jobject convertInteger (JNIEnv *env, jint i)
{
    // Convert the jint to Java's Integer class.
    jclass cls = env->FindClass( "java/lang/Integer" );
    jmethodID methodID = env->GetMethodID( cls, "<init>", "(I)V" );
    jobject ret = env->NewObject( cls, methodID, i );
    return ret;
}

/**
 * Converts a GPS Intermediate Driver location to a Java JSR-179 Location.
 */
jobject convertLocation (JNIEnv *env, GPS_POSITION &position)
{
    // At a minimum we need latitude and longitude to be valid.
    if ( (position.dwValidFields & (GPS_VALID_LATITUDE | GPS_VALID_LONGITUDE)) == 0 )
    {
        // Return an invalid location object.
        jclass clsLocation = env->FindClass( "javax/microedition/location/LocationImpl" );
        jmethodID methodLocation = env->GetMethodID( clsLocation, "<init>", "()V" );
        jobject objLocation = env->NewObject( clsLocation, methodLocation );
        return objLocation;
    }

    // Get the GPS values.
    jdouble latitude = position.dblLatitude;
    jdouble longitude = position.dblLongitude;
    jfloat altitude = (position.dwValidFields & GPS_VALID_ALTITUDE_WRT_ELLIPSOID) ? position.flAltitudeWRTEllipsoid : -15000;
    jfloat hAccuracy = (position.dwValidFields & GPS_VALID_HORIZONTAL_DILUTION_OF_PRECISION) ? position.flHorizontalDilutionOfPrecision : -15000;
    jfloat vAccuracy = (position.dwValidFields & GPS_VALID_VERTICAL_DILUTION_OF_PRECISION) ? position.flVerticalDilutionOfPrecision : -15000;
    jfloat speed = (position.dwValidFields & GPS_VALID_SPEED) ? position.flSpeed : -15000;
    jfloat heading = (position.dwValidFields & GPS_VALID_HEADING) ? position.flHeading : -15000;

    // Create a Java QualifiedCoordinates object.
    //   QualifiedCoordinates( double latitude, double longitude, float altitude, float horizontalAccuracy, float verticalAccuracy )
    jclass clsCoordinates = env->FindClass( "javax/microedition/location/QualifiedCoordinates" );
    jmethodID methodCoordinates = env->GetMethodID( clsCoordinates, "<init>", "(DDFFF)V" );
    jobject objCoordinates = env->NewObject( clsCoordinates, methodCoordinates,
        latitude, longitude, altitude, hAccuracy, vAccuracy );

    // Create a Java LocationImpl object.
    //   LocationImpl( QualifiedCoordinates qualifiedCoordinates, float speed, float course )
    //   Note that speed is tranlated from knots to meters/second by the constructor.
    jclass clsLocation = env->FindClass( "javax/microedition/location/LocationImpl" );
    jmethodID methodLocation = env->GetMethodID( clsLocation, "<init>", "(Ljavax/microedition/location/QualifiedCoordinates;FF)V" );
    jobject objLocation = env->NewObject( clsLocation, methodLocation,
        objCoordinates, speed, heading );

    return objLocation;
}

/**
 * Gets the current state of the GPS.
 */
jint getState (JNIEnv *env)
{
    try
    {
        GPS_DEVICE state;
        memset( &state, 0, sizeof(state) );
        state.dwVersion = GPS_VERSION_1;
        state.dwSize = sizeof(state);

        // Query the state of the GPS Intermediate Driver.
        BOOL result = GPSGetDeviceState( &state );
        
        if ( result != ERROR_SUCCESS )
        {
            throwLocationExceptionIfError( env, "Could not get device state", result );
            return OUT_OF_SERVICE;
        }

        // Translate the state to the JSR-179 codes.
        jint ret = convertState( state );
        return ret;
    }
    catch (...)
    {
        throwUnhandledException( env, "Unhandled exception getting the state of the GPS Intermediate Driver." );
        return OUT_OF_SERVICE;
    }
}

/**
 * Gets the last known location from the GPS.
 */
jobject getLocation (JNIEnv *env)
{
    try
    {
        // Get the current GPS location.
        GPS_POSITION position;
        memset( &position, 0, sizeof(position) );
        position.dwVersion = GPS_VERSION_1;
        position.dwSize = sizeof(position);

        DWORD result = GPSGetPosition( 
            hGPSDevice,
            &position,
            1000,  // Maximum age in milliseconds
            0 );   // Reserved

        if ( result != ERROR_SUCCESS )
        {
            throwLocationExceptionIfError( env, "Could not get position", result );
            return NULL;
        }

        jobject objLocation = convertLocation( env, position );
        return objLocation;
    }
    catch (...)
    {
        throwUnhandledException( env, "Unhandled exception getting a location from the GPS Intermediate Driver." );
        return NULL;
    }
}


//////////////////////////////////////////////////////////////////////////
// JNI Methods
//////////////////////////////////////////////////////////////////////////

/**
 * Starts the GPS Intermediate Driver.
 */
JNIEXPORT jboolean JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_startGPS (JNIEnv *env, jobject obj)
{
    try
    {
        // Is there GPS on this device?
        BOOL gps = hasGPS();

        if ( gps == FALSE )
        {
            return FALSE;
        }

        // Create handles for events that get raised when new GPS data comes in.
        hNewLocationData = CreateEvent( NULL, FALSE, FALSE, NULL );
        if ( hNewLocationData == NULL )
        {
            throwLocationException( env, "Error creating events" );
            return FALSE;
        }

        hDeviceStateChange = CreateEvent( NULL, FALSE, FALSE, NULL );
        if ( hDeviceStateChange == NULL )
        {
            throwLocationException( env, "Error creating events" );
            return FALSE;
        }

        // Start the GPS Intermediate Driver.
        hGPSDevice = GPSOpenDevice( hNewLocationData, hDeviceStateChange, NULL, 0 );
        if ( hGPSDevice == NULL )
        {
            throwLocationException( env, "Could not start GPS" );
            return FALSE;
        }

        // The GPS was successfully started.  It should be sending data within a minute.
        return TRUE;
    }
    catch (...)
    {
        throwUnhandledException( env, "Unhandled exception starting GPS Intermediate Driver." );
        return FALSE;
    }
}

/**
 * Stops the GPS Intermediate Driver.
 */
JNIEXPORT void JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_stopGPS (JNIEnv *env, jobject obj)
{
    try
    {
        // Only stop if we've started.
        if ( hGPSDevice != NULL )
        {
            // Close our event handles.
            CloseHandle( hDeviceStateChange );
            hDeviceStateChange = NULL;

            CloseHandle( hGPSDevice );
            hGPSDevice = NULL;

            // Shut down the GPS Intermediate Driver.
            BOOL result = GPSCloseDevice( hGPSDevice );
            hGPSDevice = NULL;
            
            if ( result != ERROR_SUCCESS )
            {
                throwLocationExceptionIfError( env, "Error stopping GPS" );
                return;
            }
        }
    }
    catch (...)
    {
        throwUnhandledException( env, "Unhandled exception stopping GPS Intermediate Driver." );
    }
}

/**
 * Blocks until the GPS Intermediate Driver gets a new event.  Then
 * that is returned to Java's worker thread and passed onto the user's
 * registered LocationListener.
 */
JNIEXPORT jobject JNICALL Java_javax_microedition_location_WindowsMobileLocationProvider_getGPSEvent (JNIEnv *env, jobject obj, jint interval, jint timeout, jint maxAge)
{
    try
    {
        HANDLE handles[] = { hNewLocationData, hDeviceStateChange };
        DWORD event_raised = WaitForMultipleObjects( 2, handles, FALSE, INFINITE );

        if ( event_raised == WAIT_OBJECT_0 )
        {
            // New location data is available.
            return getLocation( env );
        }
        else if ( event_raised == WAIT_OBJECT_0 + 1 )
        {
            // The GPS changed state.
            jint state = getState( env );
            return convertInteger( env, state );
        }
        else if ( event_raised == WAIT_FAILED )
        {
            throwLocationExceptionIfError( env, "Error waiting for GPS events" );
        }
    }
    catch (...)
    {
        throwUnhandledException( env, "Unhandled exception waiting for GPS Intermediate Driver event." );
    }

    // If we got here we are shutting down this thread.
    return NULL;
}
