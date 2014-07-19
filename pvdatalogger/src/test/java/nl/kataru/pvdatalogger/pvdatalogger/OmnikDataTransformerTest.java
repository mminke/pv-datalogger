/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author morten
 *
 */
public class OmnikDataTransformerTest {

	private static final byte[] VALID_DATA1 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] VALID_DATA2 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701270a360000ffff000100b3ffff0002ffffffff0921ffffffff13890022ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000000e16681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a7d16");	
	private static final byte[] VALID_DATA3 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701250a240000ffff000100b3ffff0001ffffffff0911ffffffff138c001effffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d303031380000000000000000000000e816681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a6916");
	private static final byte[] VALID_DATA4 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701240a460000ffff000100b3ffff0001ffffffff0909ffffffff138c001cffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d303031380000000000000000000000ff16681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a8a16");
	private static final byte[] INVALID_DATA_WRONGMESSAGESTART = javax.xml.bind.DatatypeConverter.parseHexBinary("ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] INVALID_DATA_WRONGMESSAGEVERSION = javax.xml.bind.DatatypeConverter.parseHexBinary("681d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] INVALID_DATA_WRONGMESSAGELENGTH = javax.xml.bind.DatatypeConverter.parseHexBinary("6881248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	
	/**
	 * 
	 */
	@Test
	public void testValidTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		Map<String, String> result = transformer.transform(VALID_DATA1);
		
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
		Assert.assertEquals("29.7", result.get("temp"));
		Assert.assertEquals("10.16", result.get("yield_today"));
		Assert.assertEquals("92.8", result.get("yield_total"));
		Assert.assertEquals("35", result.get("pac1"));
		Assert.assertEquals("50.01", result.get("fac1"));
				
		result = transformer.transform(VALID_DATA2);
		
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
		Assert.assertEquals("29.5", result.get("temp"));
		Assert.assertEquals("34", result.get("pac1"));
		Assert.assertEquals("50.01", result.get("fac1"));
		
		result = transformer.transform(VALID_DATA3);
		
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
		Assert.assertEquals("29.3", result.get("temp"));
		Assert.assertEquals("30", result.get("pac1"));
		Assert.assertEquals("50.04", result.get("fac1"));

		result = transformer.transform(VALID_DATA4);
		
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
		Assert.assertEquals("29.2", result.get("temp"));
		Assert.assertEquals("28", result.get("pac1"));
		Assert.assertEquals("50.04", result.get("fac1"));
	}

	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	/**
	 * 
	 */
	@Test
	public void testWrongMessageStartTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Input data does not start with 0x68");

		transformer.transform(INVALID_DATA_WRONGMESSAGESTART);
	}
	
	/**
	 * 
	 */
	@Test
	public void testWrongMessageVersionTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Unknown message version received: 0x1d");

		transformer.transform(INVALID_DATA_WRONGMESSAGEVERSION);
	}

	/**
	 * 
	 */
	@Test
	public void testWrongMessageLengteTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Input data does not contain 174 bytes, actual size is 161");

		transformer.transform(INVALID_DATA_WRONGMESSAGELENGTH);
	}

}
