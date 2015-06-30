/**
 * 
 */
package nl.kataru.pvdataloader;

import com.mongodb.DBObject;


/**
 * @author morten
 *
 */
public interface PVDataRepository {
	/**
	 * Save the given object
	 * 
	 * @param object
	 */
	void save(DBObject object);
	/**
	 * Close all resources which are used in the repository
	 */
	public void close();
}
