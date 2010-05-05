package com.dcg.meneame;

import java.util.List;
import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.Feed;
import com.dcg.util.rss.FeedItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MeneameDbAdapter {
    private static final String TAG = "MeneameDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * TABLE: feed_cache
     */
    public static final String FEED_DATABASE_TABLE = "feed_cache";
    public static final String FEED_KEY_ROWID = "_id";
    public static final String FEED_KEY_TAG = "tag";
	public static final String FEED_KEY_FEED_URL = "feedUrl";
	public static final String FEED_KEY_TITLE = "title";
	public static final String FEED_KEY_DESCRIPTION = "description";
	public static final String FEED_KEY_URL = "url";
	public static final String FEED_KEY_FIRT_VISIBLE_POSITION = "firstVisible";
	public static final String FEED_KEY_ITEMS = "items";
    private static final String FEED_DATABASE_CREATE =
            "create table "+FEED_DATABASE_TABLE+" " +
    		"("+FEED_KEY_ROWID+" integer primary key autoincrement, " +
    		FEED_KEY_TAG+" text not null unique, " +
    		FEED_KEY_FIRT_VISIBLE_POSITION+" integer," +
    		FEED_KEY_ITEMS+" integer," +
    		FEED_KEY_TITLE+" text, " +
    		FEED_KEY_DESCRIPTION+" text, " +
    		FEED_KEY_URL+" tex, " +
    		FEED_KEY_FEED_URL+" text);";
    
    /**
     * TABLE: items_cache
     */
    public static final String ITEMS_DATABASE_TABLE = "items_cache";
    public static final String ITEMS_KEY_ROWID = "_id";
    public static final String ITEMS_KEY_FEEDID = "feedId";
    public static final String ITEMS_KEY_ITEMID = "itemId";
    public static final String ITEMS_KEY_LINK_ID = "link_id";
    public static final String ITEMS_KEY_COMMENT_RSS = "commentRss";
    public static final String ITEMS_KEY_TITLE = "title";
    public static final String ITEMS_KEY_VOTES = "votes";
    public static final String ITEMS_KEY_LINK = "link";
    public static final String ITEMS_KEY_DESCRIPTION = "description";
    public static final String ITEMS_KEY_CATEGORY = "category";
    public static final String ITEMS_KEY_URL = "url";
    private static final String ITEMS_DATABASE_CREATE =
        "create table "+ITEMS_DATABASE_TABLE+" " +
		"("+ITEMS_KEY_ROWID+" integer primary key autoincrement, " +
		ITEMS_KEY_FEEDID+" integer not null," +
		ITEMS_KEY_ITEMID+" integer not null," +
		ITEMS_KEY_LINK_ID+" integer not null," +
		ITEMS_KEY_COMMENT_RSS+" text not null, " +
		ITEMS_KEY_TITLE+" text not null, " +
		ITEMS_KEY_VOTES+" integer not null," +
		ITEMS_KEY_LINK+" text not null, " +
		ITEMS_KEY_DESCRIPTION+" text not null, " +
		ITEMS_KEY_CATEGORY+" text not null, " +
		ITEMS_KEY_URL+" text not null);";
    
    /**
     * TABLE: system
     */
    public static final String SYSTEM_DATABASE_TABLE = "system";
    public static final String SYSTEM_KEY_KEY = "key";
    public static final String SYSTEM_KEY_VALUE = "value";
    private static final String SYSTEM_DATABASE_CREATE =
    	"create table "+SYSTEM_DATABASE_TABLE+" " +
    	"("+SYSTEM_KEY_KEY+" text primary key unique, " +
    	SYSTEM_KEY_VALUE+" text);";
    
    /** List of tables */
    public static final String[] DATABASE_TABLES = { 
    	FEED_DATABASE_TABLE, 
    	ITEMS_DATABASE_TABLE, 
    	SYSTEM_DATABASE_TABLE};
    
    /** List of create table statements
     * NOTE: Needs to be in the same order as the table list above! 
     */
    private static final String[] DATABASE_CREATE_STATEMENTS = { 
    	FEED_DATABASE_CREATE, 
    	ITEMS_DATABASE_CREATE, 
    	SYSTEM_DATABASE_CREATE};
    
    /** Internal DB name and version */
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = ApplicationMNM.mDatabaseVersion;

    private final Context mCtx;
    
    /** Semaphore used by the activities feed worker thread */
	private static Semaphore mSemaphore = new Semaphore(1);

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            ApplicationMNM.addLogCat(TAG);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	ApplicationMNM.logCat(TAG, "Creating tables");
        	createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	ApplicationMNM.logCat(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        	conditionalDropTables(db);
            onCreate(db);
        }
    }
    
    /**
     * Will drop any table that exists
     */
    private static void conditionalDropTables( SQLiteDatabase db ) {
    	int dbTablesNum = DATABASE_TABLES.length;
    	for ( int i = 0; i < dbTablesNum; i++ ) {
    		ApplicationMNM.logCat(TAG, "Dropping table: "+DATABASE_TABLES[i]);
    		try {
    			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLES[i]);
        	} catch ( Exception e ) {
        		ApplicationMNM.warnCat(TAG, "Unabe to upgrade Meneame database: "+e.toString());
        	}
    	}
    }
    
    /**
     * Will create all tables we need
     * @param db
     */
    private static void createTables( SQLiteDatabase db ) {
    	int dbTablesNum = DATABASE_CREATE_STATEMENTS.length;
    	for ( int i = 0; i < dbTablesNum; i++ ) {
    		ApplicationMNM.logCat(TAG, "Creating table: "+DATABASE_TABLES[i]);
    		try {
    			db.execSQL(DATABASE_CREATE_STATEMENTS[i]);
        	} catch ( Exception e ) {
        		ApplicationMNM.warnCat(TAG, "Unabe to upgrade Meneame database: "+e.toString());
        	}
    	}
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public MeneameDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public MeneameDbAdapter open() throws SQLException {
    	try {
			mSemaphore.acquire();
			mDbHelper = new DatabaseHelper(mCtx);
	        mDb = mDbHelper.getWritableDatabase();
	        return this;
		} catch (InterruptedException e) {
			return null;
		}
        
    }
    
    public void close() {
    	mSemaphore.release();
        mDbHelper.close();
    }
    
    /**
     * Sets a specific system key
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValue( String key, String value ) {
    	ContentValues args = new ContentValues();
		args.put(SYSTEM_KEY_KEY, key);
		args.put(SYSTEM_KEY_VALUE, value);
		try {
		
	    	if (getSystemValue(key) != null )
	    	{
	    		// Update key!
	            return mDb.update(SYSTEM_DATABASE_TABLE, args, SYSTEM_KEY_KEY + "='" + key +"'", null) > 0;
	    	}
	    	else
	    	{
	    		// Insert key
	            return mDb.insert(SYSTEM_DATABASE_TABLE, null, args) > 0;
	    	}
		} catch( Exception e ) {
			ApplicationMNM.warnCat(TAG, "setSystemValue() failed: "+e.toString());
			return false;
		}
    }
    
    /**
     * Set a boolean system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueBool( String key, boolean value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Set an integer system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueInt( String key, int value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Set a float system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueFloat( String key, float value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Get a system value associated with a specific key. Returns 
     * null if the keys dose not exists.
     * @param key
     * @return
     */    
    public String getSystemValue( String key ) {
    	Cursor mCursor = null;
    	String data = null;
    	try {
    		
	    	mCursor = mDb.query(true, SYSTEM_DATABASE_TABLE,
	    			new String[] {SYSTEM_KEY_VALUE}, 
	    			SYSTEM_KEY_KEY + "='" + key +"'",
	            	null, null, null, null, null);
		    if (mCursor != null) 
		    {
		        mCursor.moveToFirst();
		        data = mCursor.getString(0);
		        mCursor.close();
		    }
    	}
	    catch( Exception e )
	    {
	    	// Nothing to be done
	    	ApplicationMNM.warnCat(TAG, "getSystemValue() failed: "+e.toString());
	    }
	    finally
	    {
	    	if ( mCursor != null )
	    	{
	    		mCursor.close();
	    	}
	    }
	    return data;
    }
    
    /**
     * Get a system value as a boolean value
     * @param key
     * @return
     */
    public boolean getSystemValueBool( String key, boolean defaultValue ) {
    	try {
	    	return Boolean.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		ApplicationMNM.warnCat(TAG, "getSystemValueBool() failed: "+e.toString());
    		return defaultValue;
    	}
    }
    
    /**
     * Get a system value as an int value
     * @param key
     * @return
     */
    public int getSystemValueInt( String key, int defaultValue ) {
    	try {
	    	return Integer.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		ApplicationMNM.warnCat(TAG, "getSystemValueInt() failed: "+e.toString());
    		return defaultValue;
    	}
    }
    
    /**
     * Get a system value as a float value
     * @param key
     * @return
     */
    public float getSystemValueFloat( String key, float defaultValue ) {
    	try {
	    	return Float.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		ApplicationMNM.warnCat(TAG, "getSystemValueFloat() failed: "+e.toString());
    		return defaultValue;
    	}
    }
    
    /**
     * Get the row ID of a feed
     * @param feed
     * @return
     */
    public long getFeedRowID( String id) {
    	Cursor mCursor = null;
    	long data = -1;
    	try {    		
    		mCursor = mDb.query(true, FEED_DATABASE_TABLE,
	    			new String[] {FEED_KEY_ROWID}, 
	    			FEED_KEY_TAG + "='" + id +"'",
	            	null, null, null, null, null);
		    if (mCursor != null) 
		    {
		        mCursor.moveToFirst();
		        data = mCursor.getLong(0);
		    }
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not find feed in database: "+e.toString());
    	}
    	finally
	    {
	    	if ( mCursor != null )
	    	{
	    		mCursor.close();
	    	}
	    }
	    return data;
    }
    
    /**
     * Saves the feed into the db, but only if not already in the db.
     * We always save the last position! If we mark the feed as
     * cached from the sdcard we do not save the feed ietm
     * @param feed
     * @param bSDCardCache
     * @return
     */
    public boolean saveFeed( Feed feed ) {
    	try {
    		if ( feed.getFeedID().compareTo("") != 0)
    		{
    			ApplicationMNM.logCat(TAG, "saveFeed("+feed.getFeedID()+"): Start saving process...");
		    	long rowId = getFeedRowID( feed.getFeedID() );
		    	
		    	if ( rowId == -1 )
		    	{
		    		// Add a new feed!
		    		rowId = createFeed(feed);
		    		
		    		ApplicationMNM.logCat(TAG, "  Feed created: " + rowId);
		    		
		    		if ( rowId != -1 )
		    		{		    		
			    		// Now we need to add all articles and the 
			    		addFeedItems(rowId,feed);
		    		}
		    	}
		    	else
		    	{
		    		ApplicationMNM.logCat(TAG, "  Updating feed: " + rowId);
		    		
		    		// We only update the feed data, not it's articles!
		    		updateFeed(feed,rowId);
		    	}
		    	// Set the feeds row ID!
		    	feed.setRowID(rowId);
		    	
		    	// once the feed has been cached we clear it out!
		    	feed.clearArticleList();
		    	return true;
    		}
    		return false;
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not save Feed into DB: "+e.toString());
    	}
    	return false;
    }
    
    public Feed getFeed( String id ) {
    	Feed feed = null;
    	try {
	    	long rowId = getFeedRowID( id );
	    	ApplicationMNM.logCat(TAG, "getFeed("+id+"): Start restore process...");
	    	
	    	// Check if got something to be restored
	    	if ( rowId != -1 )
	    	{
	    		// Now create the feed
	    		feed = recoverFeed( rowId );
	    		// Set the feeds row ID!
		    	feed.setRowID(rowId);
	    		// recover articles
		    	// NOTE: We do not recover them all at once!
	    		//recoverFeedItems(rowId, feed);
	    		ApplicationMNM.logCat(TAG, "  Feed found: " + rowId);
	    		ApplicationMNM.logCat(TAG, "   Items: " + feed.getArticleCount());
	    	}
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not save Feed into DB: "+e.toString());
    	}
    	
    	return feed;
    }
    
    /**
     * Delete the whole feed cache, this is just as easy as dropping and creating some tables
     * @return
     */
    public boolean deleteCompleteFeedCache() {
    	mDb.delete(FEED_DATABASE_TABLE, null, null);
    	mDb.delete(ITEMS_DATABASE_TABLE, null, null);
    	return true;
    }
    
    /**
     * Will delete the cache of a feed from the db
     * @param id
     * @return
     */
    public boolean deleteFeedCache( String id ) {
    	// Delete feed items
    	long rowId = getFeedRowID( id );
    	if ( rowId != -1 )
    	{
    		deleteFeedItems(rowId);
    		// Delete feed    	
        	return mDb.delete(FEED_DATABASE_TABLE, FEED_KEY_TAG + "='" + id+"'", null) > 0;
    	}
    	
    	// Nothing to be deleted!
    	return false;    	
    }
    
    /**
     * Add a new feed to the DB
     * @param feed
     * @return
     */
    public long createFeed(Feed feed) {
    	ContentValues initialValues = new ContentValues();
        initialValues.put(FEED_KEY_TAG, feed.getFeedID());
        initialValues.put(FEED_KEY_URL, feed.getURL());
        initialValues.put(FEED_KEY_FIRT_VISIBLE_POSITION, feed.getFirstVisiblePosition());
        initialValues.put(FEED_KEY_ITEMS, feed.getArticleCount());
        initialValues.put(FEED_KEY_TITLE, feed.getRawKeyData("title"));
        initialValues.put(FEED_KEY_DESCRIPTION, feed.getRawKeyData("description"));
        initialValues.put(FEED_KEY_FEED_URL, feed.getRawKeyData("url"));        
        
        return mDb.insert(FEED_DATABASE_TABLE, null, initialValues);
    }
    
    /**
     * Update a feed in the DB
     * @param feed
     * @param rowId
     * @return
     */
    public boolean updateFeed(Feed feed, long rowId) {
    	ContentValues args = new ContentValues();
    	args.put(FEED_KEY_TAG, feed.getFeedID());
    	args.put(FEED_KEY_URL, feed.getURL());
    	args.put(FEED_KEY_FIRT_VISIBLE_POSITION, feed.getFirstVisiblePosition());
    	args.put(FEED_KEY_ITEMS, feed.getArticleCount());
    	args.put(FEED_KEY_TITLE, feed.getRawKeyData("title"));
    	args.put(FEED_KEY_DESCRIPTION, feed.getRawKeyData("description"));
    	args.put(FEED_KEY_FEED_URL, feed.getRawKeyData("url"));
    	
    	return mDb.update(FEED_DATABASE_TABLE, args, FEED_KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Create a feed from the database
     * @return
     */
    public Feed recoverFeed( long feedRowID ) {
    	Feed feed = new Feed();    	
    	Cursor mCursor = null;
    	try {    		
    		mCursor = mDb.query(true, FEED_DATABASE_TABLE,
	    			new String[] 
    			           	{
    						FEED_KEY_TAG,
    						FEED_KEY_FEED_URL,
    						FEED_KEY_FIRT_VISIBLE_POSITION,
    						FEED_KEY_TITLE,
    						FEED_KEY_DESCRIPTION,
    						FEED_KEY_FEED_URL,
    						FEED_KEY_ITEMS
    			           	}, 
		           	FEED_KEY_ROWID + "=" + feedRowID,
		           	null, null, null, null, null);
		    if (mCursor != null) 
		    {
		        mCursor.moveToFirst();
		        feed.setIdentification(mCursor.getString(0), mCursor.getString(1));
		        feed.setFirstVisiblePosition(mCursor.getInt(2));
		        feed.setValue("title", mCursor.getString(3));
		        feed.setValue("description", mCursor.getString(4));
		        feed.setValue("url", mCursor.getString(5));
		        feed.setArticleCount(mCursor.getInt(6));
		    }
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not find feed in database: "+e.toString());
    	}
    	finally
	    {
	    	if ( mCursor != null )
	    	{
	    		mCursor.close();
	    	}
	    }    	
    	return feed;
    }
    
    /**
     * Will add all articles found in the DB to the feed
     * @param feedRowID
     * @param feed
     */
    public void recoverFeedItems( long feedRowID, Feed feed ) {
    	Cursor mCursor = null;
    	try {    		
    		mCursor = mDb.query(true, ITEMS_DATABASE_TABLE,
	    			new String[]
							{
							ITEMS_KEY_TITLE,
							ITEMS_KEY_DESCRIPTION,
							ITEMS_KEY_VOTES,
							ITEMS_KEY_URL,
							ITEMS_KEY_CATEGORY,
							FEED_KEY_DESCRIPTION,
							ITEMS_KEY_COMMENT_RSS,
							ITEMS_KEY_LINK_ID
							},
		           	ITEMS_KEY_FEEDID + "=" + feedRowID,
	           		null, null, null, null, null);
		    if (mCursor != null) 
		    {
		        mCursor.moveToFirst();		        
		        while(!mCursor.isAfterLast()) {
		        	// We found and article, add it!
		        	ArticleFeedItem feedItem = new ArticleFeedItem();
		        	feedItem.setValue("title", mCursor.getString(0));
		        	feedItem.setValue("description", mCursor.getString(1));
		        	feedItem.setValue("votes", mCursor.getInt(2));
		        	feedItem.setValue("url", mCursor.getString(3));
		        	feedItem.setList("category", mCursor.getString(4));
		        	feedItem.setValue("link", mCursor.getString(5));
		        	feedItem.setValue("commentRss", mCursor.getString(6));
		        	feedItem.setValue("link_id", mCursor.getInt(7));
		        	feed.addArticle(feedItem);
		        	// Move to next one
		        	mCursor.moveToNext();
		        }
		    }
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not find feed items in database: "+e.toString());
    	}
    	finally
	    {
	    	if ( mCursor != null )
	    	{
	    		mCursor.close();
	    	}
	    }
    }
    
    /**
     * Get a feed item from the database
     * @param feedId
     * @param itemId
     * @return
     */
    public ArticleFeedItem getFeedItem( long feedRowID, long itemID )
    {
    	Cursor mCursor = null;
    	ArticleFeedItem feedItem = null;
    	try {    		
    		mCursor = mDb.query(true, ITEMS_DATABASE_TABLE,
	    			new String[]
							{
							ITEMS_KEY_TITLE,
							ITEMS_KEY_DESCRIPTION,
							ITEMS_KEY_VOTES,
							ITEMS_KEY_URL,
							ITEMS_KEY_CATEGORY,
							FEED_KEY_DESCRIPTION,
							ITEMS_KEY_COMMENT_RSS,
							ITEMS_KEY_LINK_ID
							},
		           	ITEMS_KEY_FEEDID + "=" + feedRowID + " AND " + ITEMS_KEY_ITEMID + "=" + itemID,
	           		null, null, null, null, null);
		    if (mCursor != null) 
		    {
		        mCursor.moveToFirst();
		        
	        	// We found and article, add it!
		        feedItem = new ArticleFeedItem();
	        	feedItem.setValue("title", mCursor.getString(0));
	        	feedItem.setValue("description", mCursor.getString(1));
	        	feedItem.setValue("votes", mCursor.getInt(2));
	        	feedItem.setValue("url", mCursor.getString(3));
	        	feedItem.setList("category", mCursor.getString(4));
	        	feedItem.setValue("link", mCursor.getString(5));
	        	feedItem.setValue("commentRss", mCursor.getString(6));
	        	feedItem.setValue("link_id", mCursor.getInt(7));
	        	
		    }
    	} catch( Exception e ) {
    		ApplicationMNM.warnCat(TAG,"Can not find feed item in database: "+e.toString());
    	}
    	finally
	    {
	    	if ( mCursor != null )
	    	{
	    		mCursor.close();
	    	}
	    }
    	return feedItem;
    }
    
    /**
     * Add all articles of a feed into the DB, will clean them up first
     * @param feedRowID
     * @param feed
     */
    public void addFeedItems( long feedRowID, Feed feed ) {
    	// Clear any previous articles
    	deleteFeedItems( feedRowID );
    	
    	// Add the new ones!
    	List<FeedItem> feedItems = feed.getArticleList();
    	int feedItemNum = feed.getArticleCount();    	
    	for(int i = 0; i < feedItemNum; i++ )
    	{
    		// Add feed item!
    		long rowId = addFeedItem(feedRowID, i, feedItems.get(i));
    		if ( rowId != -1 )
    		{
    			ApplicationMNM.logCat(TAG, " ["+rowId+"] Article("+i+")");
    		}
    	}
    }
    
    /**
     * Delete all feed items related to a feed
     * @param feedRowId
     */
    public boolean deleteFeedItems( long feedRowId ) {
    	return mDb.delete(ITEMS_DATABASE_TABLE, ITEMS_KEY_FEEDID + "=" + feedRowId, null) > 0;
    }
    
    /**
     * Add a feed item to the database
     * @return
     */
    public long addFeedItem( long feedRowId, int itemId, FeedItem feedItem ) {
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(ITEMS_KEY_FEEDID, feedRowId);
    	initialValues.put(ITEMS_KEY_ITEMID, itemId);    	
        initialValues.put(ITEMS_KEY_TITLE, feedItem.getRawKeyData("title"));
        initialValues.put(ITEMS_KEY_DESCRIPTION, feedItem.getRawKeyData("description"));
        initialValues.put(ITEMS_KEY_VOTES, Integer.parseInt(feedItem.getRawKeyData("votes")));
        initialValues.put(ITEMS_KEY_URL, feedItem.getRawKeyData("url"));
        initialValues.put(ITEMS_KEY_CATEGORY, feedItem.getRawKeyData("category"));
        initialValues.put(ITEMS_KEY_LINK, feedItem.getRawKeyData("link"));
        initialValues.put(ITEMS_KEY_COMMENT_RSS, feedItem.getRawKeyData("commentRss"));
        initialValues.put(ITEMS_KEY_LINK_ID, Integer.parseInt(feedItem.getRawKeyData("link_id")));
        
        return mDb.insert(ITEMS_DATABASE_TABLE, null, initialValues);
    }
}
