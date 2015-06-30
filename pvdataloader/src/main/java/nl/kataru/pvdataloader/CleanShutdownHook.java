package nl.kataru.pvdataloader;

/**
 * A helper class to try to shutdown gracefully when a crtl-c signal is send to the application
 * @author morten
 *
 */
public class CleanShutdownHook extends Thread {
	private PVDataRepository dataRepository;
	/**
	 * @param listener
	 */
	public CleanShutdownHook( PVDataRepository dataRepository ) {
		this.dataRepository = dataRepository;
	}
	
	@Override
	public void run() {
		super.run();
		
		dataRepository.close();
	}
}