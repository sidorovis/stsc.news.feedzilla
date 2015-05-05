package stsc.news.feedzilla;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FeedzillaSubcategoriesToDownloadTest {

	@Test
	public void testFeedzillaSubcategoriesToDownload() throws IOException {
		final FeedzillaSubcategoriesToDownload std = new FeedzillaSubcategoriesToDownload();
		Assert.assertTrue(std.isValid("Blogs", "Celebrities"));
		Assert.assertFalse(std.isValid("Jobs", "NotExists"));
	}

	@Test
	public void testFeedzillaSubcategoriesToDownloadAllVariables() throws IOException {
		final FeedzillaSubcategoriesToDownload std = new FeedzillaSubcategoriesToDownload();
		Assert.assertEquals(13949, std.toString().length());
	}
}
