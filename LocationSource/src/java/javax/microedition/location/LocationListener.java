package javax.microedition.location;

/**
 * The <code>LocationListener</code> represents a listener that receives events
 * associated with a particular <code>LocationProvider</code>. Applications implement
 * this interface and register it with a <code>LocationProvider</code> to obtain
 * regular position updates.
 * <p>
 * When the listener is registered with a <code>LocationProvider</code> with some
 * update period, the implementation shall attempt to provide updates at the
 * defined interval. If it isn't possible to determine the location, e.g.
 * because of the <code>LocationProvider</code> being <code>TEMPORARILY_UNAVAILABLE</code>
 * or <code>OUT_OF_SERVICE</code> or because the update period is too frequent for
 * the location method to provide updates, the implementation can send an update
 * to the listener that contains an 'invalid' <code>Location</code> instance.
 * <p>
 * The implementation shall use best effort to post the location updates at the
 * specified interval, but this timing is not guaranteed to be very exact (i.e.
 * this is not an exact timer facility for an application).
 * <p>
 * The application is responsible for any possible synchronization needed in the
 * listener methods.
 * <p>
 * The listener methods MUST return quickly and should not perform any extensive
 * processing. The method calls are intended as triggers to the application.
 * Application should do any necessary extensive processing in a separate thread
 * and only use these methods to initiate the processing.
 * 
 * @see LocationProvider
 * @see Location
 */
public interface LocationListener
{
	/**
	 * Called by the <code>LocationProvider</code> to which this listener is
	 * registered. This method will be called periodically according to the
	 * interval defined when registering the listener to provide updates of the
	 * current location.
	 * <p>
	 * The implementation shall use best effort to post the location updates at
	 * the specified interval, but this timing is not guaranteed to be very
	 * exact (i.e. this is not an exact timer facility for an application).
	 * <p>
	 * The application is responsible for any possible synchronization needed in
	 * the listener methods.
	 * <p>
	 * The listener methods MUST return quickly and should not perform any
	 * extensive processing. The method calls are intended as triggers to the
	 * application. Application should do any necessary extensive processing in
	 * a separate thread and only use these methods to initiate the processing.
	 * 
	 * @param provider - the source of the event.
	 * @param location - the location to which the event relates, i.e. the new
	 *        position.
	 */
	public void locationUpdated (LocationProvider provider, Location location);

	/**
	 * Called by the <code>LocationProvider</code> to which this listener is
	 * registered if the state of the <code>LocationProvider</code> has changed.
	 * <p>
	 * These provider state changes are delivered to the application as soon as
	 * possible after the state of a provider changes. The timing of these
	 * events is not related to the period of the location updates.
	 * <p>
	 * If the application is subscribed to receive periodic location updates, it
	 * will continue to receive these regardless of the state of the
	 * <code>LocationProvider</code>. If the application wishes to stop receiving
	 * location updates for an unavailable provider, it should de-register
	 * itself from the provider.
	 * 
	 * @param provider - the source of the event
	 * @param newState - the new state of the <code>LocationProvider</code>. This
	 *        value is one of the constants for the state defined in the
	 *        <code>LocationProvider</code> class.
	 */
	public void providerStateChanged (LocationProvider provider, int newState);
}
