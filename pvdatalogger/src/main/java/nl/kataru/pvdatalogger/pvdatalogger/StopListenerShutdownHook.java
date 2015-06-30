package nl.kataru.pvdatalogger.pvdatalogger;

/**
 * A helper class to try to shutdown gracefully when a crtl-c signal is send to the application
 * @author morten
 *
 */
public class StopListenerShutdownHook extends Thread {
	private OmnikDataListener listener;
	/**
	 * @param listener
	 */
	public StopListenerShutdownHook( OmnikDataListener listener ) {
		this.listener = listener;
	}
	
	@Override
	public void run() {
		super.run();
		
		listener.stop();
	}
}