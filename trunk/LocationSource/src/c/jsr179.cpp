// jsr179.cpp : Defines the entry point for the DLL application.
//

#include <windows.h>
#include <commctrl.h>
#include "javax_microedition_location_WindowsMobileLocationProvider.h"

/**
 * TODO : Enable Assisted part of A-GPS for some phones.
 *        The assisted part lets a the GPS acquire a fix much more quickly (10 seconds vs. 45).
 *        However, I'm not sure the accuracy ever comes down to what it is with a normal fix.
 *        For example I saw it on the Samsung BlackJack II have an HDOP of around 85 whereas
 *        that is more like 5 with the long GPS fix.
 *
 * Configures GPS Intermediate Driver registry settings.  Some phones from carriers
 * come without this to disable assisted GPS functionality except on their applications.
 * This enables it for use by all applications.
 *
void configureGPSID ()
{
    setRegistryInteger( _T("HKLM\System\CurrentControlSet\GPS Intermediate Driver"), _T("IsEnabled"), 1 );
    setRegistryInteger( _T("HKLM\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer\ActiveDevice"), _T("Index"), 4 );
    setRegistryString(  _T("HKLM\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer\ActiveDevice"), _T("Prefix"), _T("COM") );

    if ( model == "SGH-i617" )  // Samsung BlackJack II
    {
        setRegistryString(  _T("HKLM\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer"), _T("DriverInterface"), _T("COM4:") );
    }
    else if ( model == "Q9h" )  // Motorola Q9h
    {
        setRegistryString(  _T("HKLM\System\CurrentControlSet\GPS Intermediate Driver\Multiplexer"), _T("DriverInterface"), _T("COM3:") );
    }
}
*/

/**
 * DLL entry point.  Called when Java loads this DLL.
 */
BOOL APIENTRY DllMain( HANDLE hModule, DWORD ul_reason_for_call, LPVOID lpReserved )
{
	switch ( ul_reason_for_call )
    {
	    case DLL_PROCESS_ATTACH:
            break;
	    case DLL_THREAD_ATTACH:
		    break;
	    case DLL_THREAD_DETACH:
		    break;
	    case DLL_PROCESS_DETACH:
            // Stop the GPS Intermediate Driver.
		    Java_javax_microedition_location_WindowsMobileLocationProvider_stopGPS( NULL, NULL );
		    break;
	}

    // The DLL was loaded.
	return TRUE;
}
