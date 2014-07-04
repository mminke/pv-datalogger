/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author morten
 *
 */
public class OmnikDataTransformer {

	public Map<String, String> transform(byte[] rawData) {
		if( rawData.length != 170 )
			throw new IllegalArgumentException("Input data does not contain 170 bytes, actual size is " + rawData.length);
		
		Map<String, String> result = new HashMap<String, String>();
		
		String stringData = new String(rawData);
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);
				
		result.put("inverter_serialnumber", stringData.substring(15, 31));
		result.put("inverter_temp", "" + transformShort(byteBuffer, 31, 10));
		result.put("inverter_vpv1", "" + transformShort(byteBuffer, 33,10));
		result.put("inverter_vpv2", "" + transformShort(byteBuffer, 35,10));
		result.put("inverter_vpv3", "" + transformShort(byteBuffer, 37,10));
		result.put("inverter_ipv1", "" + transformShort(byteBuffer, 39,10));
		result.put("inverter_ipv2", "" + transformShort(byteBuffer, 41,10));
		result.put("inverter_ipv3", "" + transformShort(byteBuffer, 43,10));
		result.put("inverter_iac1", "" + transformShort(byteBuffer, 45,10));
		result.put("inverter_iac2", "" + transformShort(byteBuffer, 47,10));
		result.put("inverter_iac3", "" + transformShort(byteBuffer, 49,10));
		result.put("inverter_vac1", "" + transformShort(byteBuffer, 51,10));
		result.put("inverter_vac2", "" + transformShort(byteBuffer, 53,10));
		result.put("inverter_vac3", "" + transformShort(byteBuffer, 55,10));
		result.put("inverter_fac1", "" + transformShort(byteBuffer, 57,10));
		result.put("inverter_pac1", "" + transformShort(byteBuffer, 59,1));	// Do not divide pac value
		result.put("inverter_fac2", "" + transformShort(byteBuffer, 61,10));
		result.put("inverter_pac2", "" + transformShort(byteBuffer, 63,1));	// Do not divide pac value
		result.put("inverter_fac3", "" + transformShort(byteBuffer, 65,10));
		result.put("inverter_pac3", "" + transformShort(byteBuffer, 67,1));	// Do not divide pac value
		result.put("yield_today", "" + transformShort(byteBuffer, 69,100));
		result.put("yield_total", "" + transformInteger(byteBuffer, 71,10));
		result.put("inverter_htotal", "" + transformInteger(byteBuffer, 75,1));
		result.put("firmware_version_main", stringData.substring(97, 112));
		result.put("firmware_version_slave", stringData.substring(117, 126));
		result.put("message", stringData.substring(151, 166));

		return result;
	}
		
	private BigDecimal transformShort( ByteBuffer byteBuffer, int offset, int divisor) {
		short value = byteBuffer.getShort(offset);
		
		return new BigDecimal(value).divide(new BigDecimal(divisor));		
	}
	
	private BigDecimal transformInteger( ByteBuffer byteBuffer, int offset, int divisor) {
		int value = byteBuffer.getInt(offset);
		
		return new BigDecimal(value).divide(new BigDecimal(divisor));		
	}

}