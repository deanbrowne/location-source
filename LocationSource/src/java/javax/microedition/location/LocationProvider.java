package javax.microedition.location;

/**
 * This is the starting point for applications using this API and represents a
 * source of the location information. A <code>LocationProvider</code> represents a
 * location-providing module, generating <code>Location</code>s.
 * <p>
 * Applications obtain <code>LocationProvider</code> instances (classes implementing
 * the actual functionality by extending this abstract class) by calling the
 * factory method. It is the responsibility of the implementation to return the
 * correct <code>LocationProvider</code>-derived object.
 * <p>
 * Applications that need to specify criteria for the location provider
 * selection, must first create a <code>Criteria</code> object, and pass it to the
 * factory method. The methods that access the location related information
 * shall throw <code>SecurityException</code> if the application does not have the
 * relevant permission to access the location information.
 */
public abstract class LocationProvider
{
	/**
	 * Availability status code: the location provider is available.
	 */
	public static final int AVAILABLE = 1;

	/**
	 * Availability status code: the location provider is temporarily
	 * unavailable. Temporary unavailability means that the method is
	 * unavailable due to reasons that can be expected to possibly change in the
	 * future and the provider to become available. An example is not being able
	 * to receive the signal because the signal used by the location method is
	 * currently being obstructed, e.g. when deep inside a building for
	 * satellite based methods. However, a very short transient obstruction of
	 * the signal should not cause the provider to toggle quickly between
	 * <code>TEMPORARILY_UNAVAILABLE</code> and <code>AVAILABLE</code>.
	 */
	public static final int TEMPORARILY_UNAVAILABLE = 2;

	/**
	 * Availability status code: the location provider is out of service. Being
	 * out of service means that the method is unavailable and the
	 * implementation is not able to expect that this situation would change in
	 * the near future. An example is when using a location method implemented
	 * in an external device and the external device is detached.
	 */
	public static final int OUT_OF_SERVICE = 3;

	/**
	 * The last instance of a location provider.  It is obtained when the user
	 * calls <code>getInstance</code>.
	 */
	private static LocationProvider instance;
	
	/**
	 * Empty constructor to help implementations and extensions. This is not
	 * intended to be used by applications. Applications should not make
	 * subclasses of this class and invoke this constructor from the subclass.
	 */
	protected LocationProvider ()
	{
	}
	
	/**
	 * This factory method is used to get an actual <code>LocationProvider</code>
	 * implementation based on the defined criteria. The implementation chooses
	 * the <code>LocationProvider</code> so that it best fits the defined criteria,
	 * taking into account also possible implementation dependent preferences of
	 * the end user. If no concrete <code>LocationProvider</code> could be created
	 * that typically can match the defined criteria but there are other
	 * location providers not meeting the criteria that could be returned for a
	 * more relaxed criteria, <code>null</code> is returned to indicate this. The
	 * <code>LocationException</code> is thrown, if all supported location providers
	 * are out of service.
	 * <p>
	 * A <code>LocationProvider</code> instance is returned if there is a location
	 * provider meeting the criteria in either the available or temporarily
	 * unavailable state. Implementations should try to select providers in the
	 * available state before providers in temporarily unavailable state, but
	 * this can't be always guaranteed because the implementation may not always
	 * know the state correctly at this point in time. If a <code>LocationProvider</code>
	 * meeting the criteria can be supported but is currently out of service, it
	 * shall not be returned.
	 * <p>
	 * When this method is called with a <code>Criteria</code> that has all fields
	 * set to the default values (i.e. the least restrictive criteria possible),
	 * the implementation shall return a <code>LocationProvider</code> if there is
	 * any provider that isn't in the out of service state. Passing <code>null</code>
	 * as the parameter is equal to passing a <code>Criteria</code> that has all
	 * fields set to the default values, i.e. the least restrictive set of
	 * criteria.
	 * <p>
	 * This method only makes the selection of the provider based on the
	 * criteria and is intended to return it quickly to the application. Any
	 * possible initialization of the provider is done at an implementation
	 * dependent time and MUST NOT block the call to this method.
	 * <p>
	 * This method may, depending on the implementation, return the same
	 * <code>LocationProvider</code> instance as has been returned previously from
	 * this method to the calling application, if the same instance can be used
	 * to fulfil both defined criteria. Note that there can be only one
	 * <code>LocationListener</code> associated with a <code>LocationProvider</code>
	 * instance.
	 * 
	 * @param criteria - the criteria for provider selection or <code>null</code> to
	 *        indicate the least restrictive criteria with default values
	 * @return a <code>LocationProvider</code> meeting the defined criteria or
	 *         <code>null</code> if a <code>LocationProvider</code> that meets the defined
	 *         criteria can't be returned but there are other supported
	 *         available or temporarily unavailable providers that do not meet
	 *         the criteria.
	 * @throws LocationException - if all <code>LocationProvider</code>s are
	 *         currently out of service.
	 * @see Criteria
	 */
	public static LocationProvider getInstance (Criteria criteria)
		throws LocationException, SecurityException
	{
		instance = WindowsMobileLocationProvider.instance();
		return instance;
	}
	
	/**
	 * Returns the current state of this <code>LocationProvider</code>. The return
	 * value shall be one of the availability status code constants defined in
	 * this class.
	 * 
	 * @return the availability state of this </code>LocationProvider</code>
	 */
	public abstract int getState ();

	/**
	 * Retrieves a Location with the constraints given by the <code>Criteria</code>
	 * associated with this class.  If no result could be retrieved, a
	 * <code>LocationException</code> is thrown.  If the location can't be determined
	 * within the <code>timeout</code> period specified in the parameter, the method
	 * shall throw a <code>LocationException</code>.
	 * <p>
	 * If the provider is temporarily unavailable, the implementation shall wait and
	 * try to obtain the location until the timeout expires.  If the provider is out
	 * of service, then the <code>LocationException</code> is thrown immediately.
	 * <p>
	 * Note that the individual <code>Location</code> returned might not fulfil
	 * exactly the criteria used for selecting this <code>LocationProvider</code>. 
	 * The <code>Criteria</code> is used to select a location provider that typically
	 * is able to meet the defined criteria, but not necessarily for every individual
	 * location measurement.
	 * 
	 * @param timeout - a timeout value in seconds. -1 is used to indicate that the 
	 *  implementation shall use its default timeout value for this provider.
	 * @return a <code>Location</code> object 
	 * @throws LocationException - if the location couldn't be retrieved or if the
	 *  timeout period expired
	 * @throws InterruptedException - if the operation is interrupted by calling
	 *  <code>reset()</code> from another thread
	 * @throws SecurityException - if the calling application does not have a 
	 *  permission to query the location information
	 * @throws IllegalArgumentException - if the timeout = 0 or timeout < -1
	 */
	public abstract Location getLocation (int timeout)
		throws LocationException, InterruptedException;

	/**
	 * Adds a <code>LocationListener</code> for updates at the defined interval. The
	 * listener will be called with updated location at the defined interval.
	 * The listener also gets updates when the availablilty state of the
	 * <code>LocationProvider</code> changes.
	 * <p>
	 * Passing in -1 as the interval selects the default interval which is
	 * dependent on the used location method. Passing in 0 as the interval
	 * registers the listener to only receive provider status updates and not
	 * location updates at all.
	 * <p>
	 * Only one listener can be registered with each <code>LocationProvider</code>
	 * instance. Setting the listener replaces any possibly previously set
	 * listener. Setting the listener to <code>null</code> cancels the registration
	 * of any previously set listener.
	 * <p>
	 * The implementation shall initiate obtaining the first location result
	 * immediately when the listener is registered and provide the location to
	 * the listener as soon as it is available. Subsequent location updates will
	 * happen at the defined interval after the first one. If the specified
	 * update interval is smaller than the time it takes to obtain the first
	 * result, the listener shall receive location updates with invalid
	 * Locations at the defined interval until the first location result is
	 * available.
	 * <p>
	 * The timeout parameter determines a timeout that is used if it's not
	 * possible to obtain a new location result when the update is scheduled to
	 * be provided. This timeout value indicates how many seconds the update is
	 * allowed to be provided late compared to the defined interval. If it's not
	 * possible to get a new location result (interval + timeout) seconds after
	 * the previous update, the update will be made and an invalid <code>Location</code>
	 * instance is returned. This is also done if the reason for the inability
	 * to obtain a new location result is due to the provider being temporarily
	 * unavailable or out of service. For example, if the interval is 60 seconds
	 * and the timeout is 10 seconds, the update must be delivered at most 70
	 * seconds after the previous update and if no new location result is
	 * available by that time the update will be made with an invalid
	 * <code>Location</code> instance.
	 * <p>
	 * The <code>maxAge</code> parameter defines how old the location result is
	 * allowed to be provided when the update is made. This allows the
	 * implementation to reuse location results if it has a recent location
	 * result when the update is due to be delivered. This parameter can only be
	 * used to indicate a larger value than the normal time of obtaining a
	 * location result by a location method. The normal time of obtaining the
	 * location result means the time it takes normally to obtain the result
	 * when a request is made. If the application specifies a time value that is
	 * less than what can be realized with the used location method, the
	 * implementation shall provide as recent location results as are possible
	 * with the used location method. For example, if the interval is 60
	 * seconds, the <code>maxAge</code> is 20 seconds and normal time to obtain the
	 * result is 10 seconds, the implementation would normally start obtaining
	 * the result 50 seconds after the previous update. If there is a location
	 * result otherwise available that is more recent than 40 seconds after the
	 * previous update, then the <code>maxAge</code> setting to 20 seconds allows to
	 * return this result and not start obtaining a new one.
	 * 
	 * @param locationlistener - the listener to be registered. If set to <code>null</code>
	 *        the registration of any previously set listener is cancelled.
	 * @param interval - the interval in seconds. -1 is used for the default
	 *        interval of this provider. 0 is used to indicate that the
	 *        application wants to receive only provider status updates and not
	 *        location updates at all.
	 * @param timeout - timeout value in seconds, must be greater than 0. if the
	 *        value is -1, the default timeout for this provider is used. Also,
	 *        if the interval is -1 to indicate the default, the value of this
	 *        parameter has no effect and the default timeout for this provider
	 *        is used. If the interval is 0, this parameter has no effect.
	 * @param maxAge - maximum age of the returned location in seconds, must be
	 *        greater than 0 or equal to -1 to indicate that the default maximum
	 *        age for this provider is used. Also, if the interval is -1 to
	 *        indicate the default, the value of this parameter has no effect
	 *        and the default maximum age for this provider is used. If the
	 *        interval is 0, this parameter has no effect.
	 * @throws java.lang.IllegalArgumentException - if <code>interval</code> &lt; -1,
	 *         or if (<code>interval</code> != -1) and (<code>timeout</code> &gt;
	 *         <code>interval</code> or <code>maxAge</code> &gt; <code>interval</code> or (<code>timeout</code>
	 *         &lt; 1 and <code>timeout</code> != -1) or (<code>maxAge</code> &lt; 1 and
	 *         <code>maxAge</code> != -1))
	 * @throws java.lang.SecurityException - if the calling application does not
	 *         have a permission to query the location information
	 */
	public abstract void setLocationListener (
			LocationListener locationlistener, int interval, int timeout, int maxAge);
	
	/**
	 * Resets the <code>LocationProvider</code>.
	 * <p>
	 * All pending synchronous location requests will be aborted and any blocked
	 * <code>getLocation</code> method calls will terminate with
	 * <code>InterruptedException</code>.
	 * <p>
	 * Applications can use this method e.g. when exiting to have its threads freed
	 * from blocking synchronous operations.
	 */
	public abstract void reset ();
	
	/**
	 * Returns the last known location that the implementation has.  This is the
	 * best estimate that the implementation has for the previously known location.
	 * <p>
	 * Applications can use this method to obtain the last known location and check
	 * the timestamp and other fields to determine if this is recent enough and good
	 * enough for the application to use without needing to make a new request for
	 * the current location.
	 * 
	 * @return a location object. <code>null</code> is returned if the implementation
	 *  doesn't have any previous location information.
	 * @throws SecurityException - if the calling application does not have a
	 *  permission to query the location information
	 */
	public static Location getLastKnownLocation ()
	{
		if ( instance == null )
		{
			return null;
		}
		else
		{
			return instance.getLastKnownLocationToProvider();
		}
	}

	/**
	 * Returns the last known location by the provider.  This is a helper method for
	 * JSR-179's <code>static</code> method <code>getLastKnownLocation</code>.
	 * 
	 * @return a location object. <code>null</code> is returned if the implementation
	 *  doesn't have any previous location information.
	 * @throws SecurityException - if the calling application does not have a
	 *  permission to query the location information
	 * 
	 * @see #getLastKnownLocation()
	 */
	protected abstract Location getLastKnownLocationToProvider ();
}
