# Introduction #

This page details how to work on Location Source.  Check out the installation page to see how to get it onto a phone

# What You'll Need #

Location Source is a Java wrapper over native APIs.  Currently the only native implementation is against the [Windows Mobile GPS Intermediate Driver](http://msdn2.microsoft.com/en-us/library/ms850332.aspx).  As such you'll need both Java and C++ tools.

## Java ##

Location Source implements the [JSR-179 Location API](http://jcp.org/aboutJava/communityprocess/final/jsr179/index.html).  That API must run under J2ME, a subset of the standard J2SE distribution.  To ensure that Location Source is built under a J2ME-compatible environment (even though it can also run under J2SE).

For details on setting up a J2ME environment refer to the [J4ME Setup Guide](http://code.google.com/p/j4me/wiki/Setup).  J4ME is another open source project and includes a GPS example that will use Location Source.

[Ant](http://ant.apache.org/) is also required.  A build script is included that does the Location Source packaging for you.

## Windows Mobile (C++) ##

Windows Mobile supports several languages.  Location Source just uses the simplest C++ (no MFC, ATL, etc.).  This keeps Location Source both small and easy to debug.

Location Source's C++ code is built with Visual Studio 2005.  Newer versions should also work.

The GPS Intermediate Driver appeared in the Windows Mobile 5.0 API.  Location Source uses the [Windows Mobile 5.0 SDK for Smartphone](http://www.microsoft.com/downloads/details.aspx?familyid=DC6C00CB-738A-4B97-8910-5CD29AB5F8D9&displaylang=en); however, newer versions or the Pocket PC SDKs should work too.  You will need to download and install this if you haven't already.

# Building #

If everything is installed correctly you should be able to open up a command prompt in the Location Source directory and type "ant".  The Ant script then goes through the following process:
  1. Compiles all of the Java source files
  1. Generates C++ JNI header files for the Java to call into the C++
  1. Compiles all of the C++ (against the JNI header files)
  1. Jars together the Java class files
  1. Copies everything into the /dist directory

Once your build is successful follow the installation guide to most the files under /dist onto your phone!