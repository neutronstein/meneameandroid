package com.dcg.util.rss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;

import com.dcg.app.ApplicationMNM;

/**
 * A parsed feed of items
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class Feed extends FeedItem {
	
	/** log tag for this class */
	private static final String TAG = "Feed";
	
	/** List of articles */
	private List<FeedItem> mArticles=null;
	
	/** The feeds row ID */
	private long mRowID;
	
	/** This is the maximum number of data one article will hold */
	private int mMaxItemData;
	
	/** ID this feed will use to identify it self in the database*/
	private String mID = "";
	
	/** The source URL this feed uses */
	private String mURL = "";
	
	/** First visible position */
	private int mFirstVisiblePosition;
	
	/** 
	 * Number of items this feed has, take into account that once cached
	 * the item list will be cleared to only hold the visible items in the list
	 */
	private int mItemCount;
	
	/**
	 * Empty constructor
	 */
	public Feed() {
		super();		
		ApplicationMNM.addLogCat(TAG);
		mArticles = new ArrayList<FeedItem>();
		mMaxItemData = 0;
		
		mPermittedList.add("title");
	}
	
	public void setRowID( long rowID ) {
		mRowID = rowID;
	}
	
	public long getRowID() {
		return mRowID;
	}
	
	/**
	 * Set last position
	 * @param lastPosition
	 */
	public void setFirstVisiblePosition( int position ) {
		mFirstVisiblePosition = position;
	}
	
	/**
	 * Return the currently set last position
	 * @return
	 */
	public int getFirstVisiblePosition() {
		return mFirstVisiblePosition;
	}
	
	/**
	 * Sets the database identification for the feed
	 * @param id
	 * @param url
	 */
	public void setIdentification( String id, String url ) {
		mID = id;
		mURL = url;
	}
	
	/**
	 * Return ID for this feed
	 * @return
	 */
	public String getFeedID() {
		return mID;
	}
	
	/**
	 * return the URL the feed comes from
	 * @return
	 */
	public String getURL() {
		return mURL;
	}

    public int describeContents() {
		return 0;
	}
 
	/**
	 * Adds a new article int the feed
	 * @param article
	 */
	public void addArticle( FeedItem article ) {
		if ( article != null )
		{
			ApplicationMNM.logCat(TAG, "Adding article: " + article.getKeyData("title"));
			mArticles.add(mArticles.size(), article);
			
			// Compute item data
			int articleSize = article.size();
			if ( mMaxItemData < articleSize)
			{
				mMaxItemData = articleSize;
			}
			
			// This is only valid once we are adding items!
			setArticleCount(mArticles.size());
		}
	}
	
	/**
	 * Return the current max number of data each child items has
	 * @return
	 */
	public int getMaxItemData() {
		return mMaxItemData;
	}
	
	/**
	 * Clears the whole article list out
	 */
	public void clearArticleList() {
		mArticles.clear();
	}
	
	/**
	 * Returns a list of registered articles
	 * @return
	 */
	public List<FeedItem> getArticleList() {
		return mArticles;
	}
	
	/**
	 * Return an article of this feed
	 * @param position
	 * @return
	 */
	public FeedItem getArticle(int position) {
		if ( mArticles.size() > 0 && position < mArticles.size() )
			return mArticles.get(position);
		return null;
	}
	
	/**
	 * Get number of feeds we got
	 */
	public int getArticleCount() {
		// It is not save to acces the list, because we could have cached the articles
		// and so we can not asure that the list has any items!
		return mItemCount;
	}
	
	/**
	 * Update the count of items we expect to be in the list. Used when creating
	 * a feed from a real resource (an online rss) and when we load it from a
	 * cached source.
	 * @param newCount
	 */
	public void setArticleCount( int newCount ) {
		mItemCount = newCount;
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
	public boolean isKeyListValue( String key )
	{
		return super.isKeyListValue(key);
	}
}
