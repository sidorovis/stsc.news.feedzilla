package stsc.news.feedzilla.export.csv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import stsc.news.feedzilla.FeedzillaFileStorage;
import stsc.news.feedzilla.FeedzillaFileStorageReceiver;
import stsc.news.feedzilla.file.schema.FeedzillaFileArticle;
import stsc.news.feedzilla.file.schema.FeedzillaFileSubcategory;

final class FeedzillaToCsvExporter implements FeedzillaFileStorageReceiver {

	private static final class FeedzillaFileSubcategoryComparator implements Comparator<FeedzillaFileSubcategory> {

		@Override
		public int compare(FeedzillaFileSubcategory arg0, FeedzillaFileSubcategory arg1) {
			String lc = arg0.getCategory().getEnglishCategoryName();
			String rc = arg1.getCategory().getEnglishCategoryName();
			if (lc != rc) {
				return lc.compareTo(rc);
			}
			String ls = arg0.getEnglishSubcategoryName();
			String rs = arg1.getEnglishSubcategoryName();
			if (ls != rs) {
				return ls.compareTo(rs);
			}
			return 0;
		}

	}

	private final OutputStreamWriter out;
	private TreeMap<FeedzillaFileSubcategory, Metric> metrics = new TreeMap<>(new FeedzillaFileSubcategoryComparator());

	private final boolean deleteOutput = true;

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
			if (deleteOutput) {
				new File(settings.getOutputFileName()).delete();
			} else {
				throw new IllegalArgumentException("output file " + settings.getOutputFileName() + " exists, please choose another one");
			}
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
			if (e.getValue().isValidated()) {
				out.append(e.getValue().toString()).append("\n");
			}
		}
	}

}
