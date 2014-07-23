/**
 * 
 */
package nl.kataru.pvdatalogger.pvdatalogger;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author morten
 *
 */
public class OmnikDataListener {
	private static final Logger LOG = LoggerFactory.getLogger(OmnikDataListener.class);
	private int port;
	private String listenerBindingAddress;
	private OmnikDataTransformer transformer;
	private CamelContext context;

	/**
	 * @param port
	 */
	public OmnikDataListener(String listenerBindingAddress, int port) {
		this.port = port;
		this.listenerBindingAddress = listenerBindingAddress;

		SimpleRegistry registry = new SimpleRegistry();
		registry.put("omnikdecoder", new OmnikDecoder());

		context = new DefaultCamelContext(registry);
	}

	@Sharable
	public class OmnikDecoder extends FrameDecoder {

		protected Object decode(ChannelHandlerContext ctx, Channel channel,
				ChannelBuffer buffer) throws Exception {

			if( buffer.readableBytes() < 2) {
				// Not enough data received to determine the message version, wait for more data
				return null;
			}
			
			// Determine the framesize which depends on the messages first bytes?!?
			int frameSize = 0;
			if( buffer.getByte(0) != 0x68 ) {
				LOG.error("Invalid message received. Message did not start with 0x68");
				
				byte[] output = bufferToByteArray(buffer, buffer.readableBytes());
				String decodedValue = javax.xml.bind.DatatypeConverter
						.printHexBinary(output);
				LOG.error(decodedValue);
				
				return null;
			}
			
			int messageVersion = buffer.getByte(1) & 0xFF; // messageVersion & 0xFF turns the signed byte into its unsigned value in a integer
			if( messageVersion == 0x7D) { 
				frameSize = 170;
			}
			if( messageVersion == 0x81) {
				frameSize = 174;
			}

			// If the frameSize = 0 no 
			if( frameSize == 0 ) {
				LOG.error("Invalid message received. Could not determine message version. Version byte is 0x" +  Integer.toHexString(messageVersion));
			}
			
			// Start decoding the message
			if (buffer.readableBytes() < frameSize) {
				LOG.debug("Did not receive complete message yet. Current frame size is "
								+ buffer.readableBytes());
				return null;
			}
			if (buffer.readableBytes() > frameSize) {
				LOG.error("Invalid message received. Frame size to big. Expected " + frameSize + " but current frame size is "
								+ buffer.readableBytes());

				byte[] output = bufferToByteArray(buffer, buffer.readableBytes());
				String decodedValue = javax.xml.bind.DatatypeConverter
						.printHexBinary(output);
				LOG.error(decodedValue);
				
//				buffer.skipBytes(buffer.readableBytes());
				return null;
			}

			byte[] result = bufferToByteArray(buffer, frameSize);

			// Print the Hex string for debugging purposes
			String decodedValue = javax.xml.bind.DatatypeConverter
					.printHexBinary(result);
			LOG.debug(decodedValue);
			// ---------------------
			
			return result;
		}
		
		private byte[] bufferToByteArray(ChannelBuffer buffer, int size) {
			byte[] result = new byte[size];
			while(buffer.readableBytes() > 0 ) {
				result[size - buffer.readableBytes()] = buffer.readByte();
			}
			return result;
		}
		
		@Override
		public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
				throws Exception {
			
			LOG.debug("Channel is closed");
			super.channelClosed(ctx, e);
		}
		
		@Override
		public void channelDisconnected(ChannelHandlerContext ctx,
				ChannelStateEvent e) throws Exception {
			
			LOG.debug("Channel is disconnected");
			super.channelDisconnected(ctx, e);
		}
		
		
	}

	/**
	 * Initialise the Camel context with a tcp listener
	 * 
	 * @param action
	 */
	private void initialiseContext(Consumer<Map<String, String>> action) {
		if (transformer == null) {
			throw new NullPointerException("Transformer is not initialised");
		}

		RouteBuilder builder = new RouteBuilder() {
			public void configure() {
				from(
						"netty:tcp://" + listenerBindingAddress + ":" + port
								+ "?decoder=#omnikdecoder&sync=false").setExchangePattern(ExchangePattern.InOnly).process(
						new Processor() {
							public void process(Exchange exchange)
									throws Exception {

								Object body = exchange.getIn().getBody();
								if (body instanceof byte[]) {
									byte[] rawData = (byte[]) exchange.getIn()
											.getBody();

									Map<String, String> parsedData = transformer
											.transform(rawData);

									action.accept(parsedData);
								} else {
									LOG.error("ERROR, wrong data format received: "
													+ body);
								}
							}
						});
			}
		};

		// Add the route to the context
		try {
			context.addRoutes(builder);
		} catch (Exception exception) {
			LOG.error("Could not initialize Camel route.", exception );
		}
	}

	/**
	 * Start the listener
	 */
	public void run(Consumer<Map<String, String>> action) {

		initialiseContext(action);

		// Start the Camel context (The context runs async, as long as the
		// application lives)
		try {
			context.start();
		} catch (Exception exception) {
			LOG.error("Could not start Camel context.", exception );
		}
	}

	/**
	 * Stop the listener
	 */
	public void stop() {
		try {
			context.stop();
		} catch (Exception exception) {
			LOG.error("Could not stop Camel context.", exception );
		}
	}

	/**
	 * Set the transformer to use to convert the input data into a structured
	 * dataset
	 * 
	 * @param transformer
	 */
	public void setTransformer(OmnikDataTransformer transformer) {
		this.transformer = transformer;
	}
}
