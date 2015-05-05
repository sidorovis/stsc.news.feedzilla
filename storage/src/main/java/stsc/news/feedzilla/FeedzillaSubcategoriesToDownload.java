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
public final class FeedzillaSubcategoriesToDownload {

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
			if (categoryName.equals(o.getCategoryName())) {
				return subcategoryName.compareTo(o.getSubcategoryName());
			} else {
				return categoryName.compareTo(o.getCategoryName());
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((categoryName == null) ? 0 : categoryName.hashCode());
			result = prime * result + ((subcategoryName == null) ? 0 : subcategoryName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AvailableSubcategory other = (AvailableSubcategory) obj;
			if (categoryName == null) {
				if (other.categoryName != null)
					return false;
			} else if (!categoryName.equals(other.categoryName))
				return false;
			if (subcategoryName == null) {
				if (other.subcategoryName != null)
					return false;
			} else if (!subcategoryName.equals(other.subcategoryName))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return categoryName + "::" + subcategoryName;
		}

	}

	private final TreeSet<String> categories = new TreeSet<>();
	private final TreeSet<AvailableSubcategory> subcategories = new TreeSet<>();

	public FeedzillaSubcategoriesToDownload() throws IOException {
		final InputStream is = this.getClass().getResourceAsStream(filename);
		try (BufferedReader in = new BufferedReader(new InputStreamReader(is))) {
			String line = in.readLine();
			while (line != null) {
				final String[] params = line.split("\t");
				if (params.length == 4) {
					categories.add(params[1]);
					subcategories.add(new AvailableSubcategory(params[1], params[3]));
				}
				line = in.readLine();
			}
		}
	}

	public boolean isValidCategory(String categoryName) {
		return categories.contains(categoryName);
	}

	public boolean isValidSubcategory(String categoryName, String subCategoryName) {
		return subcategories.contains(new AvailableSubcategory(categoryName, subCategoryName));
	}

	@Override
	public String toString() {
		String result = "";
		for (AvailableSubcategory as : subcategories) {
			result += as.toString() + "\n";
		}
		return result;
	}
}
