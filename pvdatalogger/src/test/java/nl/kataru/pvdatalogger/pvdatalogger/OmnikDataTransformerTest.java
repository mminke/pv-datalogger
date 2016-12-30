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

	private static final byte[] VALID_DATA_VERSION_7D_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] VALID_DATA_VERSION_7D_2 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701270a360000ffff000100b3ffff0002ffffffff0921ffffffff13890022ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000000e16681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a7d16");	
	private static final byte[] VALID_DATA_VERSION_81_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530012909A10000FFFF000000B4FFFF0001FFFFFFFF0900FFFFFFFF138C000AFFFFFFFFFFFFFFFF0247000001630000004F000100000000FFFF000000000000000000000000D9684E4C312D56312E302D303036372D34000000000056312E362D303032310000000000000000000000FD16681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16");
	private static final byte[] VALID_DATA_VERSION_81_2 = javax.xml.bind.DatatypeConverter.parseHexBinary("688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530012509130000FFFF0000009DFFFF0001FFFFFFFF0900FFFFFFFF138D0000FFFFFFFFFFFFFFFF02470000016300000050000100000000FFFF000000000000000000000000DD124E4C312D56312E302D303036372D34000000000056312E362D303032310000000000000000000000FA16681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16");
	private static final byte[] INVALID_DATA_WRONGMESSAGESTART = javax.xml.bind.DatatypeConverter.parseHexBinary("ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] INVALID_DATA_WRONGMESSAGEVERSION = javax.xml.bind.DatatypeConverter.parseHexBinary("681d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] INVALID_DATA_WRONGMESSAGELENGTH = javax.xml.bind.DatatypeConverter.parseHexBinary("6881248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@Test
	public void testValidTransformationWithMessageVersion7D()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		Map<String, String> result = transformer.transform(VALID_DATA_VERSION_7D_1);
		
		Assert.assertNotNull(result);	
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("29.7", result.get("temp"));
		Assert.assertEquals("266.1", result.get("vpv1"));
		Assert.assertEquals("0", result.get("vpv2"));
		Assert.assertEquals("-1", result.get("vpv3"));
		Assert.assertEquals("0.1", result.get("ipv1"));
		Assert.assertEquals("17.9", result.get("ipv2"));	//TODO: Something wrong here: what value is this? Seems not to be ipv2!
		Assert.assertEquals("-1", result.get("ipv3"));
		Assert.assertEquals("0.2", result.get("iac1"));
		Assert.assertEquals("-1", result.get("iac2"));
		Assert.assertEquals("-1", result.get("iac3"));
		Assert.assertEquals("230.5", result.get("vac1"));
		Assert.assertEquals("-1", result.get("vac2"));
		Assert.assertEquals("-1", result.get("vac3"));
		Assert.assertEquals("50.01", result.get("fac1"));
		Assert.assertEquals("35", result.get("pac1"));
		Assert.assertEquals("-1", result.get("fac2"));
		Assert.assertEquals("-1", result.get("pac2"));
		Assert.assertEquals("-1", result.get("fac3"));
		Assert.assertEquals("-1", result.get("pac3"));
		Assert.assertEquals("10.16", result.get("yield_today"));
		Assert.assertEquals("92.8", result.get("yield_total"));
		Assert.assertEquals("164", result.get("hours_total"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
				
		result = transformer.transform(VALID_DATA_VERSION_7D_2);
		
		Assert.assertNotNull(result);		
		Assert.assertEquals("NLDN202013953027", result.get("serialnumber"));
		Assert.assertEquals("29.5", result.get("temp"));
		Assert.assertEquals("261.4", result.get("vpv1"));
		Assert.assertEquals("0", result.get("vpv2"));
		Assert.assertEquals("-1", result.get("vpv3"));
		Assert.assertEquals("0.1", result.get("ipv1"));
		Assert.assertEquals("17.9", result.get("ipv2"));	//TODO: Something wrong here: what value is this? Seems not to be ipv2! still same value as first resultset!
		Assert.assertEquals("-1", result.get("ipv3"));
		Assert.assertEquals("0.2", result.get("iac1"));
		Assert.assertEquals("-1", result.get("iac2"));
		Assert.assertEquals("-1", result.get("iac3"));
		Assert.assertEquals("233.7", result.get("vac1"));
		Assert.assertEquals("-1", result.get("vac2"));
		Assert.assertEquals("-1", result.get("vac3"));
		Assert.assertEquals("50.01", result.get("fac1"));
		Assert.assertEquals("34", result.get("pac1"));
		Assert.assertEquals("-1", result.get("fac2"));
		Assert.assertEquals("-1", result.get("pac2"));
		Assert.assertEquals("-1", result.get("fac3"));
		Assert.assertEquals("-1", result.get("pac3"));
		Assert.assertEquals("10.16", result.get("yield_today"));
		Assert.assertEquals("92.8", result.get("yield_total"));
		Assert.assertEquals("164", result.get("hours_total"));
		Assert.assertEquals("NL1-V1.0-0043-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
	}

	@Test
	public void testValidTransformationWithMessageVersion81()
	{
		Map<String, String> result;
		
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		result = transformer.transform(VALID_DATA_VERSION_81_1);
		
		Assert.assertNotNull(result);		
		Assert.assertEquals("NLDN2020145H2050", result.get("serialnumber"));
		Assert.assertEquals("29.7", result.get("temp"));
		Assert.assertEquals("246.5", result.get("vpv1"));
		Assert.assertEquals("0", result.get("vpv2"));
		Assert.assertEquals("-1", result.get("vpv3"));
		Assert.assertEquals("0", result.get("ipv1"));
		Assert.assertEquals("18", result.get("ipv2"));	//TODO: Something wrong here: what value is this? Seems not to be ipv2! still same value as first resultset!
		Assert.assertEquals("-1", result.get("ipv3"));
		Assert.assertEquals("0.1", result.get("iac1"));
		Assert.assertEquals("-1", result.get("iac2"));
		Assert.assertEquals("-1", result.get("iac3"));
		Assert.assertEquals("230.4", result.get("vac1"));
		Assert.assertEquals("-1", result.get("vac2"));
		Assert.assertEquals("-1", result.get("vac3"));
		Assert.assertEquals("50.04", result.get("fac1"));
		Assert.assertEquals("10", result.get("pac1"));
		Assert.assertEquals("-1", result.get("fac2"));
		Assert.assertEquals("-1", result.get("pac2"));
		Assert.assertEquals("-1", result.get("fac3"));
		Assert.assertEquals("-1", result.get("pac3"));
		Assert.assertEquals("5.83", result.get("yield_today"));
		Assert.assertEquals("35.5", result.get("yield_total"));
		Assert.assertEquals("79", result.get("hours_total"));
		Assert.assertEquals("NL1-V1.0-0067-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0021", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));

		result = transformer.transform(VALID_DATA_VERSION_81_2);
		
		Assert.assertNotNull(result);		
		Assert.assertEquals("NLDN2020145H2050", result.get("serialnumber"));
		Assert.assertEquals("29.3", result.get("temp"));
		Assert.assertEquals("232.3", result.get("vpv1"));
		Assert.assertEquals("0", result.get("vpv2"));
		Assert.assertEquals("-1", result.get("vpv3"));
		Assert.assertEquals("0", result.get("ipv1"));
		Assert.assertEquals("15.7", result.get("ipv2"));	//TODO: Something wrong here: what value is this? Seems not to be ipv2! still same value as first resultset!
		Assert.assertEquals("-1", result.get("ipv3"));
		Assert.assertEquals("0.1", result.get("iac1"));
		Assert.assertEquals("-1", result.get("iac2"));
		Assert.assertEquals("-1", result.get("iac3"));
		Assert.assertEquals("230.4", result.get("vac1"));
		Assert.assertEquals("-1", result.get("vac2"));
		Assert.assertEquals("-1", result.get("vac3"));
		Assert.assertEquals("50.05", result.get("fac1"));
		Assert.assertEquals("0", result.get("pac1"));
		Assert.assertEquals("-1", result.get("fac2"));
		Assert.assertEquals("-1", result.get("pac2"));
		Assert.assertEquals("-1", result.get("fac3"));
		Assert.assertEquals("-1", result.get("pac3"));
		Assert.assertEquals("5.83", result.get("yield_today"));
		Assert.assertEquals("35.5", result.get("yield_total"));
		Assert.assertEquals("80", result.get("hours_total"));
		Assert.assertEquals("NL1-V1.0-0067-4", result.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0021", result.get("firmware_version_slave"));
		Assert.assertEquals("DATA SEND IS OK\r\n", result.get("message"));
	}

	@Test
	public void testWrongMessageStartTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Input data does not start with 0x68");

		transformer.transform(INVALID_DATA_WRONGMESSAGESTART);
	}
	
	@Test
	public void testWrongMessageVersionTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Unknown message version received: 0x1d");

		transformer.transform(INVALID_DATA_WRONGMESSAGEVERSION);
	}

	@Test
	public void testWrongMessageLengteTransformation()
	{
		OmnikDataTransformer transformer = new OmnikDataTransformer();
		
		expectedEx.expect(IllegalArgumentException.class);
		expectedEx.expectMessage("Input data does not contain 174 bytes, actual size is 161");

		transformer.transform(INVALID_DATA_WRONGMESSAGELENGTH);
	}

}
