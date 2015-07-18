/**
 *
 */
package nl.kataru.pvdataloader;

import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mongodb.DBObject;

/**
 * @author morten
 *
 */
public class CSVDataTransformerTest {

	// Helper member to check for certain exception messages
	@Rule
	public ExpectedException expectedEx = ExpectedException.none();

	@SuppressWarnings("rawtypes")
	@Test
	public void testTransform() {
		final CSVDataTransformer transformer = new CSVDataTransformer();

		final String csv = "2015-06-24 21:28:04 063,NLDN2020145H2050,NL1-V1.0-0067-4,V1.6-0021,null,null,9.21,2320.6,4076,28.3,253.4,0,-1,0.1,17.9,-1,0.1,-1,-1,230.4,-1,-1,50.03,22,-1,-1,-1,-1,DATA SEND IS OK,null,null,688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530011B09E60000FFFF000100B3FFFF0001FFFFFFFF0900FFFFFFFF138B0016FFFFFFFFFFFFFFFF039900005AA600000FEC000100000000FFFF000000000000000000000000D7B94E4C312D56312E302D303036372D34000000000056312E362D3030323100000000000000000000002916681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16";

		final DBObject result = transformer.transform(csv);

		Assert.assertNotNull(result);
		Assert.assertNotNull(result.get("inverter"));
		final DBObject inverterData = (DBObject) result.get("inverter");
		Assert.assertEquals("NLDN2020145H2050", inverterData.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0067-4", inverterData.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0021", inverterData.get("firmware_version_slave"));

		Assert.assertNotNull(result.get("totals"));
		final DBObject totalsData = (DBObject) result.get("totals");
		Assert.assertEquals(9.21F, totalsData.get("yield_today"));
		Assert.assertEquals(2320.6F, totalsData.get("yield_total"));
		Assert.assertEquals(4076F, totalsData.get("hours_total"));

		Assert.assertNotNull(result.get("groups"));
		final List groups = (List) result.get("groups");
		Assert.assertEquals(3, groups.size());

		final DBObject group1 = (DBObject) groups.get(0);
		Assert.assertNotNull(group1);
		Assert.assertEquals(1, group1.get("groupNumber"));
		Assert.assertEquals(253.4F, group1.get("vpv"));
		Assert.assertEquals(0.1F, group1.get("ipv"));
		Assert.assertEquals(0.1F, group1.get("iac"));
		Assert.assertEquals(230.4F, group1.get("vac"));
		Assert.assertEquals(50.03F, group1.get("fac"));
		Assert.assertEquals(22.0F, group1.get("pac"));

		Assert.assertNotNull(result.get("system"));
		final DBObject systemData = (DBObject) result.get("system");
		Assert.assertEquals(28.3F, systemData.get("temp"));
		Assert.assertEquals("DATA SEND IS OK", systemData.get("message"));
		Assert.assertEquals(null, systemData.get("alarms"));
		Assert.assertEquals(null, systemData.get("data_age"));

		final String rawDataResult = "688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530011B09E60000FFFF000100B3FFFF0001FFFFFFFF0900FFFFFFFF138B0016FFFFFFFFFFFFFFFF039900005AA600000FEC000100000000FFFF000000000000000000000000D7B94E4C312D56312E302D303036372D34000000000056312E362D3030323100000000000000000000002916681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16";
		Assert.assertEquals(rawDataResult, result.get("rawdata"));

	}

	@Test
	public void testTransformWithNonCSV() {
		final CSVDataTransformer transformer = new CSVDataTransformer();

		final String csv = "Non CSV string";

		expectedEx.expect(TransformException.class);
		expectedEx.expectMessage("CSV input is not of the correct length. It should contain either 31 or 32 values.");

		transformer.transform(csv);
	}

	@Test
	public void testTransformWithInvalidTimestamp() {
		final CSVDataTransformer transformer = new CSVDataTransformer();

		final String csv = "201506-24 2128:04 063,NLDN2020145H2050,NL1-V1.0-0067-4,V1.6-0021,null,null,9.21,2320.6,4076,28.3,253.4,0,-1,0.1,17.9,-1,0.1,-1,-1,230.4,-1,-1,50.03,22,-1,-1,-1,-1,DATA SEND IS OK,null,null,688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530011B09E60000FFFF000100B3FFFF0001FFFFFFFF0900FFFFFFFF138B0016FFFFFFFFFFFFFFFF039900005AA600000FEC000100000000FFFF000000000000000000000000D7B94E4C312D56312E302D303036372D34000000000056312E362D3030323100000000000000000000002916681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16";

		expectedEx.expect(TransformException.class);
		expectedEx.expectMessage("The first value is not a correct ISO date format. It cannot be converted to a Date object.");

		transformer.transform(csv);
	}

	@Test
	public void testTransformWithInvalidNumber() {
		final CSVDataTransformer transformer = new CSVDataTransformer();

		final String csv = "2015-06-24 21:28:04 063,NLDN2020145H2050,NL1-V1.0-0067-4,V1.6-0021,null,null,9.21.7,2320.6,4076,28.3,253.4,0,-1,0.1,17.9,-1,0.1,-1,-1,230.4,-1,-1,50.03,22,-1,-1,-1,-1,DATA SEND IS OK,null,null,688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530011B09E60000FFFF000100B3FFFF0001FFFFFFFF0900FFFFFFFF138B0016FFFFFFFFFFFFFFFF039900005AA600000FEC000100000000FFFF000000000000000000000000D7B94E4C312D56312E302D303036372D34000000000056312E362D3030323100000000000000000000002916681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16";

		expectedEx.expect(TransformException.class);
		expectedEx.expectMessage("Error occured while parsing the CSV data. Error message is: multiple points");

		transformer.transform(csv);
	}

}
