package stsc.news.feedzilla.export.csv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class FeedzillaToCsvExporterTest {

	@Test
	public void testFeedzillaToCsvExporter() throws FileNotFoundException, IOException {
		new File("./output.txt").delete();
		new FeedzillaToCsvExporter(new FeedzillaToCsvSettings(new String[] { "-of=./output.txt", "-f=../test_data/feed_data/" }));
		new File("./output.txt").delete();
	}

}
