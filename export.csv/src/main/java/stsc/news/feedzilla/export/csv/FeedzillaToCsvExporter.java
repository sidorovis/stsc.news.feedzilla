package stsc.news.feedzilla.export.csv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import stsc.news.feedzilla.FeedzillaFileStorage;
import stsc.news.feedzilla.FeedzillaFileStorageReceiver;
import stsc.news.feedzilla.file.schema.FeedzillaFileArticle;
import stsc.news.feedzilla.file.schema.FeedzillaFileCategory;
import stsc.news.feedzilla.file.schema.FeedzillaFileSubcategory;

final class FeedzillaToCsvExporter implements FeedzillaFileStorageReceiver {

	private final OutputStreamWriter out;

	FeedzillaToCsvExporter(FeedzillaToCsvSettings settings) throws FileNotFoundException, IOException {
		preValidate(settings);

		try (OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(settings.getOutputFileName())))) {
			this.out = os;
			final FeedzillaFileStorage fileStorage = new FeedzillaFileStorage(settings.getFeedDataFolder(), settings.getDateBackDownloadFrom(), false);
			fileStorage.addReceiver(this);
			fileStorage.readData();
		}
	}

	private void preValidate(FeedzillaToCsvSettings settings) {
		if (new File(settings.getOutputFileName()).exists()) {
			throw new IllegalArgumentException("output file " + settings.getOutputFileName() + " exists, please choose another one");
		}
		final File feedDataFolder = new File(settings.getFeedDataFolder());
		if (!feedDataFolder.exists() || !feedDataFolder.isDirectory()) {
			throw new IllegalArgumentException("feedzilla data path " + settings.getFeedDataFolder() + " not exists or is not a directory");
		}
	}

	public final static void main(final String args[]) {
		try {
			if (args.length > 0 && args[0].equals("--help")) {
				FeedzillaToCsvSettings.printUsage();
				return;
			}
			new FeedzillaToCsvExporter(new FeedzillaToCsvSettings(args));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean addArticle(FeedzillaFileArticle article) throws IOException {
		outputCategory(article.getCategory());
		outputSubcategory(article.getSubcategory());
		outputArticle(article);
		return false;
	}

	private void outputCategory(FeedzillaFileCategory category) throws IOException {
		out.append(s(category.getEnglishCategoryName()));
	}

	private void outputSubcategory(FeedzillaFileSubcategory subcategory) throws IOException {
		out.append("\t").append(s(subcategory.getEnglishSubcategoryName()));
	}

	private void outputArticle(FeedzillaFileArticle article) throws IOException {
		out.append("\t").append(s(article.getSourceUrl())).append("\t").append(s(article.getPublishDate().toString())).append("\t")
				.append(s(article.getTitle())).append("\n");
	}

	private String s(String param) {
		return param.replaceAll("\n", "</br>").replaceAll("\t", "  ");
	}

}
