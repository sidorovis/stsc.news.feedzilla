package stsc.news.feedzilla;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class FeedzillaSubcategoriesToDownloadTest {

	@Test
	public void testFeedzillaSubcategoriesToDownload() throws IOException {
		final FeedzillaSubcategoriesToDownload std = new FeedzillaSubcategoriesToDownload();
		Assert.assertTrue(std.isValidSubcategory("Blogs", "Celebrities"));
		Assert.assertFalse(std.isValidSubcategory("Jobs", "NotExists"));
	}

	@Test
	public void testFeedzillaSubcategoriesToDownloadCategories() throws IOException {
		final FeedzillaSubcategoriesToDownload std = new FeedzillaSubcategoriesToDownload();
		Assert.assertTrue(std.isValidCategory("Blogs"));
		Assert.assertTrue(std.isValidCategory("Jobs"));
		Assert.assertFalse(std.isValidCategory("BadCategory"));
	}

	@Test
	public void testFeedzillaSubcategoriesToDownloadAllVariables() throws IOException {
		final FeedzillaSubcategoriesToDownload std = new FeedzillaSubcategoriesToDownload();
		Assert.assertEquals(13949, std.toString().length());
	}
}
