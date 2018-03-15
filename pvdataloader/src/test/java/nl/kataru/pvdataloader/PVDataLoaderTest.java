package nl.kataru.pvdataloader;

import com.mongodb.DBObject;
import nl.kataru.pvdataloader.PVDataLoader.Mode;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.UnknownHostException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author morten
 *
 */
public class PVDataLoaderTest {
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String PVDATA_1 = "2015-06-23 21:43:52 034,DATALOGGER_SN1,INVERTER_SN1,NL1-V1.0-0067-4,V1.6-0021,null,null,5.83,35.5,79,29.7,246.5,0,-1,0,18,-1,0.1,-1,-1,230.4,-1,-1,50.04,10,-1,-1,-1,-1,DATA SEND IS OK,null,null,AABBCCDDEEFFGGHHIIJJKKLLMMNNOOPPQQRRSSTTUUVVWWXXYYZZ"
			+ LINE_SEPARATOR;
	public static final String TEST_FILE1 = "src/test/resources/testdata/testdata1.log";

	/**
	 * Test basic usage of the PVDataLoader without following the file. Read a single line inputfile and check if the correct data is returned.
	 * This test does not include the transformer so, not all returned data is checked.
	 *
	 * @throws UnknownHostException
	 */
	@Test
	public void testBasicUsageWithoutTailer() {
		final PVDataRepository dataRepository = mock(PVDataRepository.class);

		final Path inputfilePath = Paths.get(TEST_FILE1);
		final PVDataLoader pvDataLoader = new PVDataLoader(inputfilePath, Mode.READ_UNTIL_EOF, dataRepository);
		pvDataLoader.start();

		final ArgumentCaptor<DBObject> dbObjectCaptor = ArgumentCaptor.forClass(DBObject.class);
		verify(dataRepository).save(dbObjectCaptor.capture());

		final DBObject dbObject = dbObjectCaptor.getValue();
		final DBObject inverter = (DBObject) dbObject.get("inverter");
		assertNotNull(inverter);
		assertEquals("INVERTER_SN1", inverter.get("serialnumber"));

		verifyNoMoreInteractions(dataRepository);
	}

	/**
	 * This test is more of an integration test. It tests the correct handling of files if follow-output is used.
	 * Because this blocks the loader from continuing, an Executor is used to start the loader in a separate thread.
	 * The test waits for the input to be read (or if a timeout occurs) and then verifies the result.
	 *
	 * @throws IOException
	 */
	@Test
	public void testBasicUsageWithTailer() throws IOException {
		final PVDataRepository dataRepository = mock(PVDataRepository.class);

		// Copy the test data file to a temporary file, because this tests needs to edit the file
		final Path sourcePath = Paths.get(TEST_FILE1);
		final Path destinationPath = Paths.get("src/test/resources/testdata/temp.log");

		Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

		final PVDataLoader pvDataLoader = new PVDataLoader(destinationPath, Mode.FOLLOW_OUTPUT, dataRepository);
		final ExecutorService executor = Executors.newFixedThreadPool(2);// .newSingleThreadExecutor();
		executor.submit(() -> {
			final String threadName = Thread.currentThread().getName();
			System.out.println("PV DataLoader Thread started: " + threadName);
			pvDataLoader.start();
		});

		wait(20, loader -> loader.getLineCounter() < 1, pvDataLoader);

		// First check if the single line present in the file is processed.
		ArgumentCaptor<DBObject> dbObjectCaptor = ArgumentCaptor.forClass(DBObject.class);
		verify(dataRepository).save(dbObjectCaptor.capture());

		DBObject dbObject = dbObjectCaptor.getValue();
		DBObject inverter = (DBObject) dbObject.get("inverter");
		assertNotNull(inverter);
		assertEquals("INVERTER_SN1", inverter.get("serialnumber"));

		// Now while the PVDataLoader is started, update the file, to see if the update gets processed.
		Files.write(destinationPath, PVDATA_1.getBytes(), StandardOpenOption.APPEND);

		wait(20, loader -> loader.getLineCounter() < 2, pvDataLoader);

		// Verify if the new line is processed correctly. Mockito also takes into account the first call, so that is why times(2) is called.
		dbObjectCaptor = ArgumentCaptor.forClass(DBObject.class);
		verify(dataRepository, times(2)).save(dbObjectCaptor.capture());

		dbObject = dbObjectCaptor.getValue();
		inverter = (DBObject) dbObject.get("inverter");
		assertNotNull(inverter);
		assertEquals("INVERTER_SN1", inverter.get("serialnumber"));

		verifyNoMoreInteractions(dataRepository);

		// Cleanup the temporary file if it exists
		Files.delete(destinationPath);
	}

	/**
	 * Test calling the PVDataLoader with a non existing file and verify the correct error message is written to stderr.
	 *
	 * @throws UnknownHostException
	 */
	@Test
	public void testWithNonExistingInputFile() {
		final PVDataRepository dataRepository = mock(PVDataRepository.class);

		final OutputStream syserr_os = followSystemErrOutput();

		final Path inputfilePath = Paths.get("does-not-exist.log");
		final PVDataLoader pvDataLoader = new PVDataLoader(inputfilePath, Mode.READ_UNTIL_EOF, dataRepository);
		pvDataLoader.start();

		// check if the error message is in the output stream
		assertEquals("File does not exist: does-not-exist.log" + LINE_SEPARATOR, syserr_os.toString());

		verifyNoMoreInteractions(dataRepository);
	}

	/**
	 * Helper method to return an OutputStream which is attached to the System.err output.
	 *
	 * @return
	 */
	private OutputStream followSystemErrOutput() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// redirect the System-output (normaly the console) to a variable
		System.setErr(new PrintStream(baos));

		return baos;
	}

	/**
	 * Helper method to return an OutputStream which is attached to the System.out output.
	 *
	 * @return
	 */
	private OutputStream followSystemOutOutput() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// redirect the System-output (normaly the console) to a variable
		System.setOut(new PrintStream(baos));

		return baos;
	}

	/**
	 * Helper method which checks if the given predicate returns true and then returns. If the given timeout seconds have passed
	 * without the predicate being succesfully tested, a fail of the test is triggered.
	 *
	 * @param seconds
	 *            The number of seconds to wait before a fail of the test is triggered.
	 * @param until
	 *            Predicate for which the method should wait until continuing.
	 * @param pvDataLoader
	 *            The pvDataLoader which is used as an input for the predicate.
	 */
	private void wait(int seconds, Predicate<PVDataLoader> until, PVDataLoader pvDataLoader) {
		final LocalDateTime timePoint = LocalDateTime.now().plusSeconds(seconds);
		while (until.test(pvDataLoader)) {
			// If 20s have passed without the line counter getting updated, something is wrong, so fail the test.
			if (LocalDateTime.now().isAfter(timePoint)) {
				fail("Input file was not processed correctly within 20s, something is wrong.");
			}
		}
	}

}
