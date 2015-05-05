package stsc.news.feedzilla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TreeSet;

/**
 * This class load feedzilla_validated_subcategories.csv file from resources.
 * And returns it as a list. Also provide possibility to check is
 * "category -> subcategory" available.
 */
class final FeedzillaSubcategoriesToDownload {

	private static final String filename = "feedzilla_validated_subcategories.csv";

	private static final class AvailableSubcategory implements Comparable<AvailableSubcategory> {
		private final String categoryName;
		private final String subcategoryName;

		public AvailableSubcategory(String categoryName, String subcategoryName) {
			this.categoryName = categoryName;
			this.subcategoryName = subcategoryName;
		}

		public String getCategoryName() {
			return categoryName;
		}

		public String getSubcategoryName() {
			return subcategoryName;
		}

		@Override
		public int compareTo(AvailableSubcategory o) {
			if (categoryName == o.getCategoryName()) {
				return subcategoryName.compareTo(o.getSubcategoryName());
			} else {
				return categoryName.compareTo(o.getCategoryName());
			}
		}

	}

	private final TreeSet<AvailableSubcategory> subcategories = new TreeSet<>();

	public FeedzillaSubcategoriesToDownload() throws IOException {
		final InputStream is = this.getClass().getResourceAsStream(filename);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
			String line = in.readLine();
			while (line != null) {
				final String[] params = line.split("\t");
				if (params.length == 4) {
					subcategories.add(new AvailableSubcategory(params[1], params[3]));
				}
				line = in.readLine();
			}
		}
	}

	public boolean isValid(String categoryName, String subCategoryName) {
		return subcategories.contains(new AvailableSubcategory(categoryName, subCategoryName));
	}
}
