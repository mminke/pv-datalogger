/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

/**
 * @author morten
 *
 */
public class OmnikDataListenerTest {
	private static final byte[] VALID_DATA1 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] VALID_DATA2 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701270a360000ffff000100b3ffff0002ffffffff0921ffffffff13890022ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000000e16681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a7d16");	
	private static final byte[] VALID_DATA3 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701250a240000ffff000100b3ffff0001ffffffff0911ffffffff138c001effffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d303031380000000000000000000000e816681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a6916");
	private static final byte[] VALID_DATA4 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701240a460000ffff000100b3ffff0001ffffffff0909ffffffff138c001cffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d303031380000000000000000000000ff16681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0a8a16");

	@Test
	public void testListenerWithValidMessage() {
		OmnikDataListener listener = new OmnikDataListener();
		listener.setTransformer(new OmnikDataTransformer());

		listener.run();

		CamelContext testContext = new DefaultCamelContext();
		ProducerTemplate template = testContext.createProducerTemplate();
		template.sendBody("netty:tcp://localhost:9999", VALID_DATA1 );

		template.sendBody("netty:tcp://localhost:9999", VALID_DATA2 );

		template.sendBody("netty:tcp://localhost:9999", VALID_DATA3 );

		template.sendBody("netty:tcp://localhost:9999", VALID_DATA4 );

		System.out.println("Finished");
	}
}
