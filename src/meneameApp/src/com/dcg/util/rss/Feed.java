package com.dcg.util.rss;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
	
	/** This is the maximum number of data one article will hold */
	private int mMaxItemData;
	
	/** ID this feed will use to identify it self in the database*/
	private String mID = "";
	
	/** The source URL this feed uses */
	private String mURL = "";
	
	/** Last visible position */
	private int mLastPosition;
	
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
	
	public Feed(Parcel in) {
		super();
		ApplicationMNM.addLogCat(TAG);		
		mArticles = new ArrayList<FeedItem>();
		mMaxItemData = 0;
		readFromParcel(in);
		
		mPermittedList.add("title");
	}
	
	/**
	 * Set last position
	 * @param lastPosition
	 */
	public void setLastPosition( int lastPosition ) {
		mLastPosition = lastPosition;
	}
	
	/**
	 * Return the currently set last position
	 * @return
	 */
	public int getLastPosition() {
		return mLastPosition;
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
     * Write all our data to a parcel
     */
    public void writeToParcel(Parcel dest, int flags) {
    	ApplicationMNM.logCat(TAG, "writeToParcel");
    	// First of all add the data of this feed
    	dest.writeInt(mItemData.size());
    	for (String s: mItemData.keySet()) {
    		dest.writeString(s);
    		dest.writeString(mItemData.get(s));
    	}
    	
    	// How many articles we got?
    	int articleCount = mArticles.size();
    	dest.writeInt(articleCount);
    	
    	// How many data each of those items hold?
    	dest.writeInt(mMaxItemData);
    	
    	// Now for each article add the class name and it's data
    	for(int i = 0; i < articleCount; i++)
    	{
    		FeedItem currentItem = (FeedItem)mArticles.get(i);
    		dest.writeString(currentItem.getClass().toString());
    		
    		Map<String, String> itemData = currentItem.getAllData();
    		int j = 0;
    		for (String s: itemData.keySet()) {
    			// Make sure we do not exceed!
    			if ( j >= mMaxItemData )
    				break;
    			// Write data
        		dest.writeString(s);
        		dest.writeString(itemData.get(s));
        		j++;
        	}
    	}
    	
    	// Clear the articles out
    	clearArticleList();
	}
	
    /**
     * Fill up all data from a parcel
     * @param in
     */
    public void readFromParcel(Parcel in) {
    	ApplicationMNM.logCat(TAG, "readFromParcel");
    	// Read the feed stuff in
    	int count = in.readInt();
    	for ( int i = 0; i < count; i++ )
    		setValue(in.readString(), in.readString());
    	
    	try {
	    	// Read the number of articles we should have
	    	int articleCount = in.readInt();
	    	
	    	// Read data size of each article
	    	mMaxItemData = in.readInt();
	    	
	    	for(int i = 0; i < articleCount; i++)
	    	{
	    		// Make a new feed item :P
	    		String className = in.readString();
	    		FeedItem currentItem = (FeedItem)Class.forName(className).newInstance();
	    		
	    		// Get article data
	    		for(int j = 0; j < mMaxItemData; j++)
	    		{
	    			currentItem.setValue(in.readString(), in.readString());
	    		}
	    		
	    		// Add article to feed
	    		addArticle(currentItem);
	    	}
    	} catch( Exception e) {
    		// Ups! If something got wrong we should get rid of any references!
    		mMaxItemData = 0;
    		clearArticleList();
    		ApplicationMNM.warnCat(TAG, "Failed to recover feed from parcel: "+e.toString());
    		e.printStackTrace();
    	}
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
		return mArticles.size();
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
