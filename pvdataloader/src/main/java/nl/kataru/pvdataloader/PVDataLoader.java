package nl.kataru.pvdataloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import com.mongodb.DBObject;

/**
 * The PVDataLoader initializes a mongo db repository and then whatches the given file for input.
 * Each line of csv data is transformed into a JSON object which is stored in the MongoDB database.
 */
public class PVDataLoader {
	private final Path inputfilePath;
	private static final CSVDataTransformer transformer = new CSVDataTransformer();

	private final PVDataRepository dataRepository;
	private final Mode mode;

	private int lineCounter = 0;

	public enum Mode {
		FOLLOW_OUTPUT, READ_UNTIL_EOF
	}

	public PVDataLoader(Path inputfilePath, Mode mode, PVDataRepository dataRepository) {

		this.dataRepository = dataRepository;

		this.inputfilePath = inputfilePath;
		this.mode = mode;

		// Add a shutdown hook to gracefully shutdown when the application is
		// interupted, eg. crtl-c is pressed
		// TODO: Change PVDataRepository to implement java.lang.AutoCloseable
		// and replace the shutdown hook with a try-with-resource statement
		Runtime.getRuntime().addShutdownHook(new CleanShutdownHook(dataRepository));
	}

	/**
	 * Start reading the input file. Either follow the input (similar as tail -f) or stop after no more data is available.
	 */
	public void start() {
		if (mode.equals(Mode.FOLLOW_OUTPUT)) {
			final Tailer tailer = new Tailer(inputfilePath.toFile(), new PVDataFileTailerListener());

			tailer.run();
		} else {
			// The stream hence file will also be closed here
			try (Stream<String> lines = Files.lines(inputfilePath)) {
				final PVDataFileTailerListener pvDataInputListener = new PVDataFileTailerListener();
				lines.forEach(line -> pvDataInputListener.handle(line));
			} catch (final NoSuchFileException exception) {
				System.err.println("File does not exist: " + exception.getMessage());
			} catch (final IOException exception) {
				System.err.println("Error reading input file: " + exception.getMessage());
			}
		}
	}

	public int getLineCounter() {
		return lineCounter;
	}

	public class PVDataFileTailerListener extends TailerListenerAdapter {
		@Override
		public void handle(String line) {
			try {
				// System.out.println("Processing data: " + line);
				final DBObject pvData = transformer.transform(line);

				dataRepository.save(pvData);
				lineCounter++;
			} catch (final TransformException exception) {
				System.err.println("WARNING: Input data ignored. Could not parse data: " + line);
				System.err.println("\tMessage is: " + exception.getMessage());
			}
		}

	}
}
