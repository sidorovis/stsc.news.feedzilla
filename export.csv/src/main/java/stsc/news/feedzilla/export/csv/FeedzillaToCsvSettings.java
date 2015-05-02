package stsc.news.feedzilla.export.csv;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;

final class FeedzillaToCsvSettings {

	private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

	private String feedDataFolder;
	private LocalDateTime dateBackDownloadFrom;
	private String outputFileName;

	public FeedzillaToCsvSettings(String[] argc) {
		final Iterator<String> argIterator = Arrays.asList(argc).iterator();
		while (argIterator.hasNext()) {
			final String parameter = argIterator.next();
			parseParameter(parameter);
		}
		validate();
	}

	private void parseParameter(String parameter) {
		if (parameter.startsWith("-f=")) {
			feedDataFolder = parameter.substring(3);
		} else if (parameter.startsWith("-df=")) {
			dateBackDownloadFrom = LocalDate.parse(parameter.substring(4), formatter).atStartOfDay();
		} else if (parameter.startsWith("-of=")) {
			outputFileName = parameter.substring(4);
		}
	}

	private void validate() {
		if (feedDataFolder == null) {
			feedDataFolder = "./";
		}
		if (dateBackDownloadFrom == null) {
			dateBackDownloadFrom = LocalDateTime.of(1990, 1, 1, 0, 0);
		}
		if (outputFileName == null) {
			outputFileName = "./feedzilla_output.csv";
		}
	}

	// getters
	public String getFeedDataFolder() {
		return feedDataFolder;
	}

	public LocalDateTime getDateBackDownloadFrom() {
		return this.dateBackDownloadFrom;
	}

	public String getOutputFileName() {
		return this.outputFileName;
	}

	// usage

	public static void printUsage() {
		System.out.println("<...>.jar -f=<feed folder path> -df=<date read from> -of=<output file>");
		System.out.println("  -  -f= by default is './'");
		System.out.println("  -  -df= format is " + formatter.toString() + " by default 01.01.1990");
		System.out.println("  -  -of= by default is './feeedzilla_output.csv'");
	}

}