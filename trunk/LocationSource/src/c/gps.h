//
// Defines GPS functions used only within the C++ code.  The functions called
// by Java are are defined by the automatically generated JNI header file.
//

/**
 * Starts the GPS Intermediate Driver.  This will fire up the GPS if another
 * process hasn't already.
 */
void startGPS ();

/**
 * Stops the GPS Intermediate Driver.  If no other processes are using it then
 * the GPS will stop thereby saving batteries.
 */
void stopGPS ();
