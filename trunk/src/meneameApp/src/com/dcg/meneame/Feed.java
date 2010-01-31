package com.dcg.meneame;

import java.util.ArrayList;
import java.util.List;

public class Feed extends Object {
	
	/** List of articles */
	private List<Article> mArticles=new ArrayList<Article>();
	
	/** Unique name of this feed, used by our caching process */
	private String nName;
	
	/**
	 * @return the nName
	 */
	public String getName() {
		return nName;
	}

	/**
	 * @param nName the nName to set
	 */
	public void setName(String nName) {
		this.nName = nName;
	}

	/**
	 * Returns a list of articles
	 * @return List
	 */
	public List<Article> getArticles() {
		return mArticles;
	}
	
	/**
	 * Adds a new article int the feed
	 * @param article
	 */
	public void addArticle( Article article ) {
		mArticles.add(0, article);
	}

}
