package stsc.news.feedzilla.export.csv;

import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Test;

public class FeedzillaToCsvSettingsTest {

	@Test
	public void testFeedzillaToCsvSettings() {
		final FeedzillaToCsvSettings settings = new FeedzillaToCsvSettings(new String[] {});
		Assert.assertEquals("./", settings.getFeedDataFolder());
		Assert.assertEquals(LocalDateTime.of(1990, 1, 1, 0, 0), settings.getDateBackDownloadFrom());
		Assert.assertEquals("./feedzilla_output.csv", settings.getOutputFileName());

	}

	@Test
	public void testFeedzillaToCsvSettingsWithData() {
		final FeedzillaToCsvSettings settings = new FeedzillaToCsvSettings(new String[] { "-of=./output.txt", "-f=../test_data/feed_data/", "-df=24.07.1987" });
		Assert.assertEquals("../test_data/feed_data/", settings.getFeedDataFolder());
		Assert.assertEquals("./output.txt", settings.getOutputFileName());
		Assert.assertEquals(LocalDateTime.of(1987, 7, 24, 0, 0), settings.getDateBackDownloadFrom());
	}
}
