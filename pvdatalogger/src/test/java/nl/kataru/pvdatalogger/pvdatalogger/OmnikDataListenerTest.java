/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
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

	private static final byte[] INVALID_DATA1 = javax.xml.bind.DatatypeConverter.parseHexBinary("b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	
	private Map<String,String> data;
		
	@Test
	public void testListenerWithValidMessage() throws UnknownHostException, IOException, InterruptedException {
		OmnikDataListener listener = new OmnikDataListener("localhost", 9998);
		listener.setTransformer(new OmnikDataTransformer());
		listener.run( d -> data = d);

		Thread.sleep(1000);
		sendData(VALID_DATA1);
		Thread.sleep(1000);
		Assert.assertNotNull(data);
		Assert.assertEquals("NLDN202013953027", data.get("serialnumber"));
		Assert.assertEquals("NL1-V1.0-0043-4", data.get("firmware_version_main"));
		Assert.assertEquals("V1.6-0018", data.get("firmware_version_slave"));
		Assert.assertEquals("92.8", data.get("yield_total"));
		Assert.assertEquals("230.5", data.get("vac1"));
		
		System.out.println(data);
		
		sendData( VALID_DATA2 );

		sendData( VALID_DATA3 );

		sendData( VALID_DATA4 );
		
		listener.stop();
	}
	
	@Test
	public void testListenerWithInValidMessage() throws UnknownHostException, IOException {
		OmnikDataListener listener = new OmnikDataListener("localhost", 9998);
		listener.setTransformer(new OmnikDataTransformer());

		listener.run( d -> data = d);

		sendData(INVALID_DATA1);
		
		// TODO: How to detect the exception which occured?!?!?!
	}
	
	/**
	 * Send the raw data over the TCP port.
	 * To prevent Camel from encoding the data automatically, the Camel ProducerTemplate is not used!
	 * 
	 * @param data
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private void sendData(byte[] data) throws UnknownHostException, IOException {
		Socket clientSocket = new Socket("localhost", 9998);   
		DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());   
		outToServer.write(data);
		
		clientSocket.close();
		
//		CamelContext testContext = new DefaultCamelContext();
//		ProducerTemplate template = testContext.createProducerTemplate();
//		template.sendBody("netty:tcp://localhost:9998", data );		
	}
}

