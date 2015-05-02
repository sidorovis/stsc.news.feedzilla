package stsc.news.feedzilla.export.csv;

import java.io.BufferedOutputStream;
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
		try (OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(settings.getOutputFileName())))) {
			this.out = os;
			final FeedzillaFileStorage fileStorage = new FeedzillaFileStorage(settings.getFeedDataFolder(), settings.getDateBackDownloadFrom(), false);
			fileStorage.addReceiver(this);
			fileStorage.readData();
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
		out.append(category.getEnglishCategoryName());
	}

	private void outputSubcategory(FeedzillaFileSubcategory subcategory) throws IOException {
		out.append("\t").append(subcategory.getEnglishSubcategoryName());
	}

	private void outputArticle(FeedzillaFileArticle article) throws IOException {
		out.append("\t").append(article.getSourceUrl()).append("\t").append(article.getPublishDate().toString()).append("\t").append(article.getTitle()).append("\n");
	}

}
