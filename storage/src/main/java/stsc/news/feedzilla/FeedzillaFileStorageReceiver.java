package stsc.news.feedzilla;

import java.io.IOException;

import stsc.news.feedzilla.file.schema.FeedzillaFileArticle;

public interface FeedzillaFileStorageReceiver {

	public boolean addArticle(FeedzillaFileArticle article) throws IOException;

}
