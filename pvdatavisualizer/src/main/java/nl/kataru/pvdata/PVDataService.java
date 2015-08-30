/**
 *
 */
package nl.kataru.pvdata;

/**
 * @author morten
 *
 */
public interface PVDataService {

	/**
	 * Retrieve the information of the inverter specified with the given id (=serial number).
	 *
	 * @param id
	 *            The serial number identifying the inverter.
	 * @return The information of the inverter.
	 */
	String getInverter(String id);

	/**
	 * Retrieve the latest information of the inverter identified by the specified id (=serial number). This information consists of the following elements:
	 * yield_std, yield_ytd, yield_today, power, voltage, temperature
	 *
	 * @param id
	 *            The serial number identifying the inverter to retrieve the actual data for.
	 * @return The actual data of the inverter.
	 */
	String getActualData(String id);

}
