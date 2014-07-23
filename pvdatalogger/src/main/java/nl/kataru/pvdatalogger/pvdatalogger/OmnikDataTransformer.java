/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author morten
 *
 */
public class OmnikDataTransformer {
	private static final DateFormat isoDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss SSS");

	public Map<String, String> transform(byte[] rawData) {
		if( rawData[0] != 0x68 )
			throw new IllegalArgumentException("Input data does not start with 0x68");

		int frameSize = 0;
		int offset = 0;
		int messageVersion = rawData[1] & 0xFF;
		if( (messageVersion) == 0x7D) {
			frameSize = 170;
			offset = 0;
		}
		if( (messageVersion) == 0x81) {
			frameSize = 174;
			offset = 4;
		}
		if( frameSize == 0)
			throw new IllegalArgumentException("Unknown message version received: 0x" + Integer.toHexString(messageVersion) );

		if( rawData.length != frameSize )
			throw new IllegalArgumentException("Input data does not contain " + frameSize + " bytes, actual size is " + rawData.length);
		
		Map<String, String> result = new HashMap<String, String>();
		
		ByteBuffer byteBuffer = ByteBuffer.wrap(rawData);
				
		Date now = new Date();
				
		result.put("timestamp", isoDateFormat.format(now));
		//TODO: Also print the hex string of the unknown data, this allows for later interpretation if new structure elements are known
		result.put("serialnumber", transformToString(byteBuffer, 15, 16));
		result.put("temp", "" + transformShort(byteBuffer, 31, 10));
		result.put("vpv1", "" + transformShort(byteBuffer, 33,10));
		result.put("vpv2", "" + transformShort(byteBuffer, 35,10));
		result.put("vpv3", "" + transformShort(byteBuffer, 37,10));
		result.put("ipv1", "" + transformShort(byteBuffer, 39,10));
		result.put("ipv2", "" + transformShort(byteBuffer, 41,10));
		result.put("ipv3", "" + transformShort(byteBuffer, 43,10));
		result.put("iac1", "" + transformShort(byteBuffer, 45,10));
		result.put("iac2", "" + transformShort(byteBuffer, 47,10));
		result.put("iac3", "" + transformShort(byteBuffer, 49,10));
		result.put("vac1", "" + transformShort(byteBuffer, 51,10));
		result.put("vac2", "" + transformShort(byteBuffer, 53,10));
		result.put("vac3", "" + transformShort(byteBuffer, 55,10));
		result.put("fac1", "" + transformShort(byteBuffer, 57,100));
		result.put("pac1", "" + transformShort(byteBuffer, 59,1));	// Do not divide pac value
		result.put("fac2", "" + transformShort(byteBuffer, 61,100));
		result.put("pac2", "" + transformShort(byteBuffer, 63,1));	// Do not divide pac value
		result.put("fac3", "" + transformShort(byteBuffer, 65,100));
		result.put("pac3", "" + transformShort(byteBuffer, 67,1));	// Do not divide pac value
		result.put("yield_today", "" + transformShort(byteBuffer, 69,100));
		result.put("yield_total", "" + transformInteger(byteBuffer, 71,10));
		result.put("hours_total", "" + transformInteger(byteBuffer, 75,1));

		result.put("firmware_version_main", transformToString(byteBuffer, 97+offset, 15));
		result.put("firmware_version_slave", transformToString(byteBuffer, 117+offset, 9));
		result.put("message", transformToString(byteBuffer, 151+offset, 17));
		result.put("rawdata", javax.xml.bind.DatatypeConverter.printHexBinary(rawData));

		return result;
	}
		
	private BigDecimal transformShort( ByteBuffer byteBuffer, int offset, int divisor) {
		short value = byteBuffer.getShort(offset);
		if( value == -1 )
			return new BigDecimal(value);
		else
			return new BigDecimal(value).divide(new BigDecimal(divisor));		
	}
	
	private BigDecimal transformInteger( ByteBuffer byteBuffer, int offset, int divisor) {
		int value = byteBuffer.getInt(offset);
		
		if( value == -1 )
			return new BigDecimal(value);
		else
			return new BigDecimal(value).divide(new BigDecimal(divisor));		
	}
	
	private String transformToString( ByteBuffer byteBuffer, int offset, int size) {
		byte[] byteData = new byte[size];
		
		for(int index=0; index < size; index++) {
			byteData[index] = byteBuffer.get(offset + index);
		}
		
		return new String(byteData);
	}

}