package nl.kataru.pvdatalogger.pvdatalogger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import org.jboss.netty.handler.codec.frame.CorruptedFrameException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author morten
 */
public class OmnikDataListenerIntegrationTest {
    private static final int MAX_WAIT_COUNTER = 100;
    private static final byte[] VALID_DATA_VERSION_7D_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("687d41b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
    private static final byte[] VALID_DATA_VERSION_81_1 = javax.xml.bind.DatatypeConverter.parseHexBinary("688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530012909A10000FFFF000000B4FFFF0001FFFFFFFF0900FFFFFFFF138C000AFFFFFFFFFFFFFFFF0247000001630000004F000100000000FFFF000000000000000000000000D9684E4C312D56312E302D303036372D34000000000056312E362D303032310000000000000000000000FD16681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16");
    private static final byte[] INVALID_DATA1 = javax.xml.bind.DatatypeConverter.parseHexBinary("b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");
    private static final byte[] TOOLONG_DATA = javax.xml.bind.DatatypeConverter.parseHexBinary("688141B09D8B755F9D8B755F8102014E4C444E323032303134354832303530012909A10000FFFF000000B4FFFF0001FFFFFFFF0900FFFFFFFF138C000AFFFFFFFFFFFFFFFF0247000001630000004F000100000000FFFF000000000000000000000000D9684E4C312D56312E302D303036372D34000000000056312E362D303032310000000000000000000000FD16681141F09D8B755F9D8B755F444154412053454E44204953204F4B0D0A2B16b0ce1d1524ce1d15248102014e4c444e32303230313339353330323701290a650000ffff000100b3ffff0002ffffffff0901ffffffff13890023ffffffffffffffff03f8000003a0000000a4000100000000ffff000000000000000000004e4c312d56312e302d303034332d34000000000056312e362d3030313800000000000000000000002016681141f0ce1d1524ce1d1524444154412053454e44204953204f4b0d0aae16");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private int nrOfMessagesReceived;
    private List<Map<String, String>> receivedMessages;
    private OmnikDataListener listener;

    @Before
    public void setUp() {
        nrOfMessagesReceived = 0;
        receivedMessages = new ArrayList<>();
        listener = new OmnikDataListener("localhost", 9998);
        listener.setTransformer(new OmnikDataTransformer());
        listener.run(message -> {
            synchronized (receivedMessages) {
                receivedMessages.add(message);
            }
        });
        waitFor(listener::isStarted);
    }

    @After
    public void tearDown() {
        listener.stop();
    }

    @Test
    public void testListenerWithMultipleValidMessages() throws IOException, InterruptedException {
        // Act
        sendData(VALID_DATA_VERSION_7D_1);
        waitFor(() -> receivedMessages.size() == 1); // Make sure each message is handled in the correct order
        sendData(VALID_DATA_VERSION_81_1);
        waitFor(() -> receivedMessages.size() == 2);

        // Assert
        assertEquals(2, receivedMessages.size());
        Map<String, String> message1 = receivedMessages.get(0);
        assertEquals("NLDN202013953027", message1.get("serialnumber"));
        assertEquals("DATA SEND IS OK\r\n", message1.get("message"));

        Map<String, String> message2 = receivedMessages.get(1);
        assertEquals("NLDN2020145H2050", message2.get("serialnumber"));
        assertEquals("DATA SEND IS OK\r\n", message2.get("message"));
    }

    @Test
    public void testListenerWithInValidMessage() throws IOException, InterruptedException {
        expectedException.expect(IllegalStateException.class);
        sendData(INVALID_DATA1);
        waitFor(() -> receivedMessages.size() > 0);
        fail("Timeout should have occured because no data should have been processed.");
    }

    @Test
    public void testListenerWithTooLongMessage() throws IOException, InterruptedException {
        expectedException.expect(IllegalStateException.class);
        sendData(TOOLONG_DATA);
        waitFor(() -> receivedMessages.size() > 0);
        fail("Timeout should have occured because no data should have been processed.");
    }

//    @Test
//    public void testListenerWithTooLongMessage2() throws IOException, InterruptedException {
//        expectedException.expect(IllegalStateException.class);
//
//        Socket clientSocket = new Socket("localhost", 9998);
//        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
//        dataOutputStream.write(TOOLONG_DATA);
//        Thread.sleep(5000);
//        dataOutputStream.write(TOOLONG_DATA);
//        Thread.sleep(5000);
//        clientSocket.close();
//        fail("Timeout should have occured because no data should have been processed.");
//    }

    private void waitFor(BooleanSupplier condition) {
        int counter = 0;
        while (!condition.getAsBoolean() && counter < MAX_WAIT_COUNTER) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException("Sleep was interupted unexpectedly while waiting.");
            }
            counter++;
        }
        if (!condition.getAsBoolean()) {
            throw new IllegalStateException("Timeout occured while waiting.");
        }
    }

    /**
     * Send the raw data over the TCP port.
     * To prevent Camel from encoding the data automatically, the Camel ProducerTemplate is not used!
     *
     * @param data
     * @throws IOException
     */
    private void sendData(byte[] data) throws IOException {
        Socket clientSocket = new Socket("localhost", 9998);
        DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        dataOutputStream.write(data);
        clientSocket.close();

//		CamelContext testContext = new DefaultCamelContext();
//		ProducerTemplate template = testContext.createProducerTemplate();
//		template.sendBody("netty:tcp://localhost:9998", data );		
    }
}

