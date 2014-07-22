/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author morten
 *
 */
public class OmnikDataListenerTest {
	private static final byte[] VALID_DATA_VERSION_7D_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	private static final byte[] VALID_DATA_VERSION_81_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530012909A10000FFFF000000B4FFFF0001FFFFFFFF0900FFFFFFFF138C000AFFFFFFFFFFFFFFFF0247000001630000004F000100000000FFFF000000000000000000000000D9684E4C312D56312E302D303036372D34000000000056312E362D303032310000000000000000000000FD16681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16");

	private static final byte[] INVALID_DATA1 = javax.xml.bind.DatatypeConverter.parseHexBinary("b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
	
	private Map<String,String> data;
		
	/**
	 * Test the listener functionality and check if a result is returned. No full transformation is tested because that is done in a separate test
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testListenerWithValidMessage() throws UnknownHostException, IOException, InterruptedException {
		OmnikDataListener listener = new OmnikDataListener("localhost", 9998);
		listener.setTransformer(new OmnikDataTransformer());
		listener.run( d -> data = d);

		Thread.sleep(1000);
		data = null;
		sendData(VALID_DATA_VERSION_7D_1);
		Thread.sleep(500);
		Assert.assertNotNull(data);
		Assert.assertEquals("NLDN202013953027", data.get("serialnumber"));
		Assert.assertEquals("DATA SEND IS OK\r\n", data.get("message"));
		
		// Test third dataset
		data = null;
		sendData( VALID_DATA_VERSION_81_1 );
		Thread.sleep(500);
		
		Assert.assertNotNull(data);		
		Assert.assertEquals("NLDN2020145H2050", data.get("serialnumber"));
		Assert.assertEquals("DATA SEND IS OK\r\n", data.get("message"));
	
		listener.stop();
	}
	
	/**
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	@Test
	public void testListenerWithInValidMessage() throws UnknownHostException, IOException, InterruptedException {
		OmnikDataListener listener = new OmnikDataListener("localhost", 9998);
		listener.setTransformer(new OmnikDataTransformer());

		listener.run( d -> data = d);

		data = null;
		sendData(INVALID_DATA1);
		Thread.sleep(500);

		// No data should be received. Application ignores invalid message. Only logging takes place
		Assert.assertNull(data);
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

