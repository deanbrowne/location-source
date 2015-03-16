# Introduction #

This page describes how to install Location Source.  Only Windows Mobile on IBM's WEME JVM is currently supported.

# Windows Mobile on IBM's WEME #

There are several Windows Mobile JVMs available.  IBM's [WebSphere Everyplace Micro Environment (WEME) 6.1.1](http://www-306.ibm.com/software/wireless/weme) is one of the best.  Many phones come pre-installed with other JVMs, but you can [download WEME](http://www-128.ibm.com/developerworks/websphere/zones/wireless/weme_eval_runtimes.html) and run it alongside them.

Once WEME is running install Location Source by:
  * Copying `jsr179-gpsid.dll` into its `bin` directory
  * Copying `LocationSource-<version>.jar` into its `lib/jclMidp20/ext` directory

Now any application can use it just by making calls to the [JSR-179 API](http://www-users.cs.umn.edu/~czhou/docs/jsr179/lapi/index.html)!

# (Optional) Running the Example #

Location Source does not include an example.  However, any JSR-179 application can be used.  We recommend the [J4ME GPS example](http://code.google.com/p/j4me/wiki/BluetoothGPS) so you can see the actual data being passed through Location Source.

To install the J4ME GPS example on WEME:
  * Run WEME's `emulator` program (found in the `bin` directory)
  * Type in the OTA installation URL for J4ME:  `http://www.scoreout.com/j4me/ota/`
  * Select the "GPS Example" midlet to run
  * When the example starts accept the default criteria for a location provider (i.e. press  "OK")
  * Depending on the type of GPS you should see data become available within a couple of minutes