/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * @author morten
 *
 */
public class OmnikDataListener {
	private static final int DEFAULT_PORT = 9999;
	private int port;
	private OmnikDataTransformer transformer; 


	/**
	 * 
	 */
	public OmnikDataListener() {
		this(DEFAULT_PORT);
	}

	/**
	 * @param port
	 */
	public OmnikDataListener(int port) {
		this.port = port;
	}


	/**
	 * Start the listener
	 */
	public void run() { // TODO: Add lambda to return receveid data!

		CamelContext context = new DefaultCamelContext();

		RouteBuilder builder = new RouteBuilder() {
			public void configure() {
				from("netty:tcp://localhost:" + port).process(
						new Processor() {
							public void process(Exchange exchange)
									throws Exception {

								System.out.println("RECEIVED DATA");
								Object body = exchange.getIn().getBody();
								if( body instanceof byte[])
								{
									byte[] rawData = (byte[])exchange.getIn().getBody();
									System.out.println("Received data " + rawData.length);
									
									
									Map<String, String> parsedData = transformer.transform(rawData);
									
									System.out.println(parsedData);
								}
								else
								{
									System.out.println("ERROR, wrong data format received: " + body);
								}
							}
						});
			}
		};

		// Add the route to the context
		try {
			context.addRoutes(builder);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		// Start the Camel context (The context runs async, as long as the application lives)
		try {
			context.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Set the transformer to use to convert the input data into a structured dataset
	 * 
	 * @param transformer
	 */
	public void setTransformer(OmnikDataTransformer transformer) {
		this.transformer = transformer;
	}
}
