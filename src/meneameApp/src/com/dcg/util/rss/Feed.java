package com.dcg.util.rss;

import java.util.ArrayList;
import java.util.List;

import com.dcg.app.ApplicationMNM;

public class Feed extends FeedItem {
	
	/** log tag for this class */
	private static final String TAG = "Feed";
	
	/** List of articles */
	private List<FeedItem> mArticles=new ArrayList<FeedItem>();
	
	/**
	 * Empty constructor
	 */
	public Feed() {
		super();
		
		ApplicationMNM.AddLogCat(TAG);
	}
	
	
	/**
	 * Adds a new article int the feed
	 * @param article
	 */
	public void addArticle( FeedItem article ) {
		if ( article != null )
		{
			ApplicationMNM.LogCat(TAG, "Adding article: " + article);
			mArticles.add(0, article);
		}
	}
	
	/**
	 * Returns a list of registered articles
	 * @return
	 */
	public List<FeedItem> getArticleList() {
		return mArticles;
	}
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key )
	{
		return super.isKeyPermitted(key);
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key )
	{
		return super.isKeyRestricted(key);
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key )
	{
		return super.isKeyListValue(key);
	}
}
