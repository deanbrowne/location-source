package javax.microedition.location;

/**
 * Location provider which glues together the Java JSR-179 Location API
 * to the Windows Mobile <a href="http://msdn2.microsoft.com/en-us/library/ms889503.aspx">
 * GPS Intermediate Driver</a> (GPSID) API.  All the C and Java code mix here.
 * 
 * @see LocationProvider
 */
class WindowsMobileLocationProvider 
	extends LocationProvider
	implements Runnable
{
	/**
	 * The singleton instance of this class.  By making it a singleton we can
	 * avoid a lot of synchronization and library loading hassles.
	 */
	private static WindowsMobileLocationProvider instance;

	/**
	 * Flag indicating if the GPS Intermediate Driver is configured or not.
	 */
	private boolean configured;
	
	/**
	 * The worker thread used to raise location events to <code>locationListener</code>.
	 * This thread will only exist so long as <code>locationListener</code> is not
	 * <code>null</code>.
	 */
	private final Thread worker = new Thread( this );

	/**
	 * The application's object registered to listen to location updates.
	 * This can be <code>null</code> meaning the application isn't receiving
	 * events. 
	 */
	private LocationListener locationListener;
	
	/**
	 * The last <code>Location</code> returned by the GPS Intermediate Driver.
	 */
	private Location lastLocation;
	
	/**
	 * The time the last location update arrived.  This is used to make sure
	 * location updates are given only at the registered <code>interval</code>.
	 */
	private long lastLocationTime;
	
	/**
	 * The last known state of the location provider.
	 */
	private int state = TEMPORARILY_UNAVAILABLE;

	/**
	 * The time between location updates in milliseconds.  If this is 0 then
	 * no location events should be raised.
	 */
	private int interval;

	/**
	 * The maximum wait time, in milliseconds, for GPS data.
	 */
	private int timeout;

	/**
	 * The maximum age, in milliseconds, GPS data can be.
	 */
	private int maxAge;

	/**
	 * Returns the singleton instance of this provider.
	 * 
	 * @return The singleton instance of this provider.
	 * @throws LocationException - if all <code>LocationProvider</code>s are
	 *         currently out of service.
	 */
	public static WindowsMobileLocationProvider instance ()
		throws LocationException
	{
		if ( instance == null )
		{
			instance = new WindowsMobileLocationProvider();
		}
		
		if ( instance.configured == false )
		{
			// No GPS is configured on this device.
			throw new LocationException( "No GPS receivers are registered with this device." );
		}
		
		return instance;
	}
	
	/**
	 * Constructs a location provider for Windows Mobile.
	 * 
	 * @throws UnsatisfiedLinkError if jsr179.dll cannot be loaded.
	 * @throws LocationException - if all <code>LocationProvider</code>s are
	 *         currently out of service.
	 */
	private WindowsMobileLocationProvider ()
		throws LocationException
	{
		// Load the native C library.
		if ( isIBM() )
		{
			try
			{
				com.ibm.oti.vm.VM.loadLibrary( "jsr179-gpsid" );
			}
			catch (Exception e)
			{
				throw new RuntimeException( "Could not load jsr179-gpsid.dll.  Is it in /bin?" );
			}
		}
		else
		{
			// Not available in MIDP 2.0.
			//System.loadLibrary( "jsr179" );
			
			throw new RuntimeException( "Only runs in IBM's WEME JVM (a.k.a. J9)." );
		}
		
		// Start the GPS.
		configured = startGPS();

		if ( configured )
		{
			// Start the background thread that captures GPS events and raises
			// them to the location listener.
			worker.start();
		}
	}
	
	/**
	 * @return <code>true</code> if this is IBM's JVM; <code>false</code>
	 *  otherwise.
	 */
	private static boolean isIBM ()
	{
		try
		{
			Class.forName( "com.ibm.oti.vm.VM" );
		}
		catch (Throwable e)  // ClassNotFoundException, NoClassDefFoundError
		{
			return false;
		}
		
		return true;
	}

	/**
	 * Sets a new listener object created by the API user.  It will start getting
	 * location updates on the <code>worker</code> thread.
	 * <p>
	 * This method is synchronized so that another thread cannot
	 * create a race condition by changing something else.
	 * 
	 * @see javax.microedition.location.LocationProvider#setLocationListener(javax.microedition.location.LocationListener, int, int, int)
	 */
	public synchronized void setLocationListener (LocationListener locationlistener, int interval, int timeout, int maxAge)
	{
		// Set the interval between location updates.
		if ( interval < 0 )
		{
			interval = 1;
		}
		
		this.interval = interval * 1000;  // Convert seconds to milliseconds

		// Set the timeout for waiting for GPS data.
		if ( timeout < 1 )
		{
			timeout = 10;
		}
		
		this.timeout = timeout * 1000;  // Convert seconds to milliseconds
		
		// Set the maximum age of usuable GPS data.
		if ( maxAge < 1 )
		{
			maxAge = 3;
		}
		
		this.maxAge = maxAge * 1000;  // Convert seconds to milliseconds
		
		// Record the new location listener.
		this.locationListener = locationlistener;
	}
	
	/**
	 * @see javax.microedition.location.LocationProvider#getLastKnownLocationToProvider()
	 */
	protected Location getLastKnownLocationToProvider ()
	{
		return lastLocation;
	}

	/**
	 * @see javax.microedition.location.LocationProvider#getLocation(int)
	 */
	public Location getLocation (int timeout)
	{
		return lastLocation;
	}

	/**
	 * Closes the GPS.
	 * <p>
	 * This method is synchronized so that another thread cannot
	 * simultaneously start GPS and create a race condition.
	 * 
	 * @see javax.microedition.location.LocationProvider#reset()
	 */
	public synchronized void reset ()
	{
		// No more location listener.
		setLocationListener( null, 0, -1, -1 );
	}
	
	/**
	 * Returns the state of the GSP Intermediate Driver.
	 * 
	 * @see javax.microedition.location.LocationProvider#getState()
	 */
	public int getState ()
	{
		return state;
	}
	
	/**
	 * Initializes the GPS Intermediate Driver.  This will start the GPS
	 * chip which will start trying to acquire a fix.
	 * 
	 * @return <code>true</code> if the GPS was started; <code>false</code>
	 *  if no is available.
	 */
	private native boolean startGPS ();

	/**
	 * Closes the GPS Intermideate Driver.  This stops the GPS and the
	 * battery power it consumes.
	 */
	protected native void stopGPS ();
	
	/**
	 * Blocks until a new GPS event is raised by the GPS Intermediate
	 * Driver.  The returned object will be one of:
	 * <ol>
	 *  <li><code>Location</code> when a new location is acquired
	 *  <li><code>Integer</code> when the GPS changes state
	 *  <li><code>null</code> when the GPSID is closing.
	 * </ol>
	 * 
	 * @param interval is the time between location updates in milliseconds.
	 * @param timeout is the wait period, in milliseconds, for location 
	 *  information before returning an invalid location object to the
	 *  location listener.
	 * @param maxAge is the maximum age, in milliseconds, of location 
	 *  information.
	 * @return A <code>Location</code> event, new state event, or <code>
	 *  null</code> signaling shutdown.
	 */
	private native Object getGPSEvent (int interval, int timeout, int maxAge);
	
	/**
	 * A background thread that posts location events to the registered
	 * <code>LocationListener</code>.  This keeps the main UI thread free.
	 * 
	 * @see Runnable#run()
	 */
	public void run ()
	{
		try
		{
			// Get location events until signaled to shutdown.
			while ( true )
			{
				// Block until a new event has been raised.
				Object o = getGPSEvent( interval, timeout, maxAge );
				
				// Check what kind of update it is.
				if ( o == null )
				{
					// Signal for this thread to exit
					break;
				}
				else if ( o instanceof Integer )
				{
					// The provider is now unavailable.
					Integer i = (Integer)o;
					int newState = i.intValue();
					
					// Forward the state change event to the user's location listener.
					if ( newState != AVAILABLE )  // to ensure all AVAILABLE states also have a new location
					{
						raiseStateChangeEvent( newState );
					}
				}
				else // ( o instanceof javax.microedition.location.Location )
				{
					// A new location event has been received;
					Location l = (Location)o;
					
					// Was the provider unavailable and is now available?
					if ( (state != AVAILABLE) && l.isValid() )
					{
						// Now the provider is available again.
						raiseStateChangeEvent( AVAILABLE );
					}
					
					// Forward the location event to the user's location listener.
					raiseLocationEvent( l );
				}
				
				// Give up the CPU.
				Thread.sleep( 100 );
			}
		}
		catch (Throwable t)
		{
			// Should never happen, but if it does it would be nice to know why.
			System.out.println( t.getMessage() );
		}
	}
	
	/**
	 * Call when the location provider gives us a new state.
	 * 
	 * @param newState is the <code>javax.microedition.location.LocationProvider</code>
	 *  state code.
	 */
	private synchronized void raiseStateChangeEvent (int newState)
	{
		// Record the unavailable state.
		state = newState;
		
		// Forward to the application's listener.
		if ( locationListener != null )
		{
			try
			{
				locationListener.providerStateChanged( this, newState );
			}
			catch (Throwable t)
			{
				// This is a programming error in the user's application.
				System.out.println( t.getMessage() );
			}
		}
	}
	
	/**
	 * Call when the location provider gives us a new location.
	 * 
	 * @param location is the new location.
	 */
	private synchronized void raiseLocationEvent (Location location)
	{
		// Record the last known location to the GPS.
		if ( (location != null) && location.isValid() )
		{
			lastLocation = location;
		}
		
		// Forward to the application's listener.
		if ( locationListener != null )
		{
			// Does the user want location events?
			if ( interval != 0 )
			{
				// Do not provide updates until the interval has expired.
				long now = System.currentTimeMillis();
				
				if ( lastLocationTime + interval <= now )
				{
					try
					{
						lastLocationTime = now;
						locationListener.locationUpdated( this, location );
					}
					catch (Throwable t)
					{
						// This is a programming error in the user's application.
						System.out.println( t.getMessage() );
					}
				}
			}
		}
	}
	
	/**
	 * @return The name of this location provider.
	 */
	public String toString ()
	{
		return "Windows Mobile GPS Intermediate Driver";
	}
}
