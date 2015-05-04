package stsc.news.feedzilla.export.csv;

import java.time.Duration;
import java.time.LocalDateTime;

import stsc.news.feedzilla.file.schema.FeedzillaFileArticle;

final class Metric {

	private final String categoryName;
	private final int categoryId;
	private final String subcategoryName;
	private final int subcategoryId;

	private boolean validated;

	private int amountOfArticles;
	private LocalDateTime firstDateTime;
	private LocalDateTime lastDateTime;

	Metric(FeedzillaFileArticle article) {
		this.categoryName = article.getCategory().getEnglishCategoryName();
		this.categoryId = article.getCategory().getId();
		this.subcategoryName = article.getSubcategory().getEnglishSubcategoryName();
		this.subcategoryId = article.getSubcategory().getId();
		this.validated = true;
		this.amountOfArticles = 0;
		this.firstDateTime = article.getPublishDate();
		this.lastDateTime = article.getPublishDate();
		processArticle(article);
	}

	public void processArticle(FeedzillaFileArticle article) {
		if (validated && categoryId != article.getCategory().getId()) {
			this.validated = false;
		}
		if (validated && subcategoryId != article.getSubcategory().getId()) {
			this.validated = false;
		}
		amountOfArticles += 1;
		if (firstDateTime.isAfter(article.getPublishDate())) {
			firstDateTime = article.getPublishDate();
		}
		if (lastDateTime.isBefore(article.getPublishDate())) {
			lastDateTime = article.getPublishDate();
		}
	}

	public boolean isValidated() {
		final long days = Duration.between(firstDateTime, lastDateTime).getSeconds() / 60 / 60 / 24;
		return validated && (1.0 * amountOfArticles / days) > 2.0;
	}

	@Override
	public String toString() {
		return categoryId + "\t" + categoryName + "\t" + subcategoryId + "\t" + subcategoryName;
	}

}