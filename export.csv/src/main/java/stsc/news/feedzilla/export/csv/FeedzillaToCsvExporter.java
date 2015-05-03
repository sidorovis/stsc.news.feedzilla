package stsc.news.feedzilla.export.csv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import stsc.news.feedzilla.FeedzillaFileStorage;
import stsc.news.feedzilla.FeedzillaFileStorageReceiver;
import stsc.news.feedzilla.file.schema.FeedzillaFileArticle;
import stsc.news.feedzilla.file.schema.FeedzillaFileSubcategory;

final class FeedzillaToCsvExporter implements FeedzillaFileStorageReceiver {

	private static final class Metric {

		private final int categoryId;
		private final int subcategoryId;
		private boolean validated;

		public Metric(FeedzillaFileArticle article) {
			this.categoryId = article.getCategory().getId();
			this.subcategoryId = article.getSubcategory().getId();
			this.validated = true;
		}

		public void processArticle(FeedzillaFileArticle article) {
			if (categoryId != article.getCategory().getId()) {
				this.validated = false;
			}
			if (subcategoryId != article.getSubcategory().getId()) {
				this.validated = false;
			}
		}

		@Override
		public String toString() {
			return String.valueOf(categoryId) + "\t" + String.valueOf(subcategoryId) + "\t" + String.valueOf(validated);
		}

	}

	private final OutputStreamWriter out;
	private HashMap<FeedzillaFileSubcategory, Metric> metrics = new HashMap<>();

	FeedzillaToCsvExporter(FeedzillaToCsvSettings settings) throws FileNotFoundException, IOException {
		preValidate(settings);

		try (OutputStreamWriter os = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(settings.getOutputFileName())))) {
			this.out = os;
			final FeedzillaFileStorage fileStorage = new FeedzillaFileStorage(settings.getFeedDataFolder(), settings.getDateBackDownloadFrom(), false);
			fileStorage.addReceiver(this);
			fileStorage.readData();

			outputData();
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
		processArticle(article);
		return false;
	}

	private void processArticle(FeedzillaFileArticle article) {
		final Metric metric = metrics.get(article.getSubcategory());
		if (metric == null) {
			metrics.put(article.getSubcategory(), new Metric(article));
		} else {
			metric.processArticle(article);
		}
	}

	private void outputData() throws IOException {
		for (Map.Entry<FeedzillaFileSubcategory, Metric> e : metrics.entrySet()) {
			out.append(s(e.getKey().getDisplaySubcategoryName())).append("\t").append(e.getValue().toString()).append("\n");
		}
	}

	private String s(String param) {
		return param.replaceAll("\n", "</br>").replaceAll("\t", "  ");
	}

}
