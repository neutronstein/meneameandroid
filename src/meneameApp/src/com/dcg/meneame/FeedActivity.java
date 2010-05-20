package com.dcg.meneame;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

import com.dcg.adapter.ArticlesAdapter;
import com.dcg.app.ApplicationMNM;
import com.dcg.dialog.AboutDialog;
import com.dcg.provider.SystemValue;
import com.dcg.rss.BaseRSSWorkerThread;
import com.dcg.rss.Feed;
import com.dcg.rss.FeedItem;
import com.dcg.task.MenealoTask;
import com.dcg.task.RequestFeedTask;
import com.dcg.task.RequestFeedTask.RequestFeedListener;
import com.dcg.task.RequestFeedTaskParams;

/**
 * Basic activity that handles feed parsing and stuff like that
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
abstract public class FeedActivity extends ListActivity implements RequestFeedListener {
	
	/** Log tag */
	private static final String TAG = "FeedActivity";
	
	/** Our RssWorkerThread class so subclasses will be able to call another one */
	protected static String mRssWorkerThreadClassName = "com.dcg.rss.RSSWorkerThread";
	protected static String mLocalRssWorkerThreadClassName = "com.dcg.rss.LocalRSSWorkerThread";

	/** Feed URL */
	protected String mFeedURL = "";
	
	/** Semaphore used by the activities feed worker thread */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** Worker thread which will do the async operations */
	private BaseRSSWorkerThread mRssThread = null;
	
	/** Worker thread which will do the async operations in our DB */
	//private ArticleDBCacheThread mDBThread = null;
	
	/** Our cached main list view */
	private ListView mListView = null;
	
	/** Handler used to communicate with our worker thread*/
	protected Handler mHandler = null;
	
	/** Codes used to inform our activity how we completed */
	public static final int COMPLETE_SUCCESSFULL = 0;
	public static final int COMPLETE_ERROR_THREAD_ALIVE = 1;
	public static final int COMPLETE_ERROR = 2;
	
	/** Refresh menu item id */
	private static final int MENU_REFRESH = 0;
	
	/** Notame menu item id */
    private static final int MENU_NOTAME = 1;
	
	/** Settings menu item id */
    private static final int MENU_SETTINGS = 2;
    
    /** About menu item id */
    private static final int MENU_ABOUT = 3;
    
    /** Sub activity ID's */
    private static final int SUB_ACT_SETTINGS_ID = 0;
    private static final int SUB_ACT_NOTAME_ID = 1;
    
    /** Context menu options */
    private static final int CONTEXT_MENU_OPEN = 0;
    private static final int CONTEXT_MENU_OPEN_SOURCE = 1;
    private static final int CONTEXT_MENU_VOTE = 2;
    
    /** Is this an article or an comments feed? */
    protected boolean mbIsArticleFeed;
    
    /** Are we paused or not? */
    protected boolean mbIsPaused;
    
    /** Are we loading a cached feed? */
    protected boolean mbIsLoadingCachedFeed;
    
    /** Current feed we got */
    private Feed mFeed = null;
    
    /** Last visible item when we entered the pause state */
    private int mFirstVisiblePosition= -1;
    
    /** Database helper */
    private MeneameDbAdapter mDBHelper = null;
    
    public FeedActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);		
		mbIsArticleFeed = true;
	}
    
    /**
     * Return the db adapter we are currently using
     * @return
     */
    public MeneameDbAdapter getDBHelper() {
    	return mDBHelper;
    }
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onCreate()");
		
		
		// Form an array specifying which columns to return. 
		String[] projection = new String[] {
				SystemValue._ID,
				SystemValue.KEY,
				SystemValue.VALUE
				};

		// Get the system value URI
		Uri AllSystemValues = ContentUris.withAppendedId(SystemValue.CONTENT_URI, 1);

		// Make the query. 
		Cursor cur = managedQuery(AllSystemValues, projection, null, null, null);
		
		// Create databse helper
		//mDBHelper = new MeneameDbAdapter(this);
		
		// Unpause
		mbIsPaused = false;
		
		// Create a new handler
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				handleThreadMessage( msg );
			}
		};
		
		// Perpare layout
		setContentView(R.layout.meneo_list);
		
		// Do final stuff
		seupListView();
		
		// Refresh if needed
		_conditionRefreshFeed();
	}
	
	@Override
	protected void onStart() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onStart()");
		super.onStart();
	}
	
	@Override
	protected void onResume() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onResume()");	
		super.onResume();
		
		// Open database
		if ( mDBHelper != null )
		{
			mDBHelper.open();
		}
		
		// Restore app state if any
		restoreState();
		
		// Unpause
		mbIsPaused = false;
		
		// Recreate the handler
		if ( mHandler == null )
		{
			mHandler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					handleThreadMessage( msg );
				}
			};
		}
		
		// Make sure or empty list text is set to it's default in case we are not loading
		// a feed already
		if ( mRssThread == null )
		{
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
		}
		else
		{
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(mbIsLoadingCachedFeed?R.string.refreshing_lable_cached:R.string.refreshing_lable);
		}
		
		// Look if we need to request a stop for the current caching thread
		if ( mbIsLoadingCachedFeed && mRssThread != null )
		{
			mRssThread.requestStop();
			mRssThread = null;
			mbIsLoadingCachedFeed = false;
		}
		
		// Should be get the feed from the cached file?
		if ( mRssThread == null || !mRssThread.isAlive() )
		{
			// If the refresh thread is active kill it in case it's a cached one
			String storageType = getStorageType();
	        if ( storageType.compareTo("None") != 0 )
	        {
	        	// Do we got a valid cache file?
	        	if ( hasCachedFeed( storageType ) )
		        	try {
						//InputStreamReader reader = new InputStreamReader(new FileInputStream ( this.getSDCardCacheFilePath()), "UTF-8");
		        		refreshFeed( true );
		        	} catch (Exception e) {
						// Not cached
					}
	        }
		}
	}
	
	@Override
	protected void onRestart() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onRestart()");
		super.onRestart();
	}
	
	@Override
	protected void onPause() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onPause()");
		
		// Save state
		saveState();
		
		// Look if we need to request a stop for the current caching thread
		if ( mbIsLoadingCachedFeed && mRssThread != null )
		{
			mRssThread.requestStop();
			mRssThread = null;
			mbIsLoadingCachedFeed = false;
		}
		
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		emptyTextView.setText("");
		
		// Pause
		mbIsPaused = true;
		
		// Clear handler
		mHandler = null;
		
		// Free feed
		if ( mFeed != null )
		{
			mFeed.clearArticleList();
		}
		mFeed = null;
		
		// Free listadapter
		setListAdapter(null);
		
		// Cleanup
		System.gc();
		
		// Close it
		if ( mDBHelper != null )
		{
			mDBHelper.close();
		}
		
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onStop()");
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onDestroy()");
		super.onDestroy();
	}
	
	/**
	 * Save state data into
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onSaveInstanceState()");
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onRestoreInstanceState()");	
	}
	
	/**
	 * Save the apps state into the database to be able to recover it again later
	 */
	private void saveState() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::saveState()");
		ApplicationMNM.logCat(TAG, " - First visible position: " + mListView.getFirstVisiblePosition());
		try {
			if ( mDBHelper != null ) 
			{
				String storageType = getStorageType();
				if ( storageType.compareTo("Internal") == 0 )
				{
					saveFeedIntoDB(false);
				}
				mDBHelper.setSystemValueInt(getTabActivityTag()+"FirstVisiblePosition", mListView.getFirstVisiblePosition());
				mDBHelper.setSystemValueBool(getTabActivityTag()+"SaveState", true);
			}
		} catch( Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed to save app state: "+e.toString());
		}
	}
	
	/**
	 * Restores a previously saved state into the database and will erase the cached
	 * data after restoring
	 */
	private void restoreState() {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::restoreState()");
		try {
			if (  mDBHelper.getSystemValueBool(getTabActivityTag()+"SaveState", false)) 
			{
				ApplicationMNM.logCat(TAG, " Starting to restore saved state!");
				mFirstVisiblePosition = mDBHelper.getSystemValueInt(getTabActivityTag()+"FirstVisiblePosition", -1);
				// We saved the app state to clear flag
				mDBHelper.setSystemValueBool(getTabActivityTag()+"SaveState", false);
			}
		} catch( Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed to restore app state: "+e.toString());
		}
	}
	
	/**
	 * Saves the current feed into the db
	 */
	public void saveFeedIntoDB( boolean bClearCache ) {
		if ( mFeed != null )
		{
			if ( bClearCache )
			{
				mDBHelper.deleteFeedCache(getIndicatorStringID());
			}
			mFeed.setFirstVisiblePosition(mListView.getFirstVisiblePosition());
			mDBHelper.saveFeed(mFeed);
		}
	}
	
	/**
	 * Reeds a feed from the database
	 * @return
	 */
	public Feed readFeedFromDB() {
		return mDBHelper.getFeed(this.getIndicatorStringID());
	}
	
	/**
	 * IF we touch the screen and we do not have any feed and no request has been
	 * made refresh the feed from the net
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onTouchEvent()");
		// If the users touches the screen and no feed is setup refresh it!
		if ( (mFeed == null || mFeed.getArticleCount() == 0) && !isRssThreadAlive() )
		{
			refreshFeed( false );
		}
		return super.onTouchEvent(event);
	}
	
	/**
	 * Look if the rss thread is currently doing something
	 * @return
	 */
	public boolean isRssThreadAlive() {
		return isThreadAlive(mRssThread);
	}
	
	/**
	 * Are we currently voting or not
	 * @return
	 */
	/*
	public boolean isMenealoThreadAlive() {
		return isThreadAlive(mMenealoThread);
	}
	/**/
	
	/**
	 * Checks if a specific thread is alife
	 * @param thread
	 * @return
	 */
	private boolean isThreadAlive( Thread thread ) {
		return (thread != null && thread.isAlive());
	}
	
	/**
	 * Should we refresh in launch or not?
	 * @return
	 */
	public boolean shouldRefreshOnLaunch() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        return prefs.getBoolean("pref_app_refreshonlaunch", false);
	}
	
	/**
	 * Refresh from an existing feed or should we start a new request?
	 */
	private void _conditionRefreshFeed() {
		if ( mFeed != null )
		{
			// We got already a feed, so just set a new adapter
			_updateFeedList();
		}
		else
		{
	    	if ( shouldRefreshOnLaunch() )
	        {
	        	refreshFeed( false );
	        }
		}
	}
	
	/**
	 * Setup ListView
	 */
	protected void seupListView() {
		mListView = getListView();
		
		if ( mListView != null )
		{
			// Set basic ListView stuff
			mListView.setTextFilterEnabled(true);
			
			// Add context menu
			mListView.setOnCreateContextMenuListener( 
					new View.OnCreateContextMenuListener() {
						public void onCreateContextMenu(ContextMenu menu, View view,ContextMenu.ContextMenuInfo menuInfo) {
							menu.add(0, CONTEXT_MENU_OPEN, 0, R.string.meneo_item_open);
							if ( mbIsArticleFeed )
							{
								menu.add(0, CONTEXT_MENU_OPEN_SOURCE, 0, R.string.meneo_item_open_source);
								menu.add(0, CONTEXT_MENU_VOTE, 0, R.string.meneo_item_vote);
							}
						}
					});
		}
		else
		{
			ApplicationMNM.warnCat(TAG,"No ListView found in layout for " + this.toString());
		}
	}
	
	protected void handleThreadMessage(Message msg) {
		Bundle data = msg.getData();
		
		// Check who has send the msg
		int msgID = data.getInt( ApplicationMNM.MSG_ID_KEY );
		int completeKey = data.getInt( ApplicationMNM.COMPLETED_KEY);
		int errorKey = data.getInt( ApplicationMNM.ERROR_KEY);
		boolean bSuccess = false;
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::handleThreadMessage() > Thread: "+ msgID +" Key: " + completeKey + " ErrorKey: " + errorKey);
		switch(msgID) {
			case ApplicationMNM.MSG_ID_ARTICLE_PARSER:				
				String errorMsg = "";
				// Check if it completed ok or not
				if ( completeKey == ApplicationMNM.COMPLETED_OK )
				{
					try {
						onRefreshCompleted(COMPLETE_SUCCESSFULL, data, (Feed) msg.obj,"");
					} catch ( ClassCastException e ) {
						errorMsg = getResources().getString(R.string.msg_obj_null);
						if ( msg.obj != null )
						{
							errorMsg = getResources().getString(R.string.msg_obj_wrong_type_unknown)+" "+ msg.obj.toString();
						}
					} finally {
						if ( errorMsg != null && !errorMsg.equals("") )
						{
							// This is an error but we will treat it here instead of the normal way!
							onRefreshCompleted(COMPLETE_ERROR, null, null, errorMsg);
						}
					}
					
					// All fine!
					bSuccess = true;
				}
				break;
			case ApplicationMNM.MSG_ID_MENEALO:
				// We finished a voting action
				bSuccess = true;
				break;
			case ApplicationMNM.MSG_ID_DB_PARSER:
				// We finished the refresh action from the DB
				bSuccess = true;
				onRefreshCompleted(COMPLETE_SUCCESSFULL, data, (Feed) msg.obj,"");
				break;
		}
		
		// If we got any error show it now!
		if ( !bSuccess )
		{
			onRefreshCompleted(COMPLETE_ERROR, null, null, getFeedErrorMessage(msgID,errorKey));
		}
	}
	
	/**
	 * returns the error string
	 * @return
	 */
	public String getFeedErrorMessage( int msgID, int errorID ) {
		int resID = R.string.error_unknown;
		switch( msgID ) {
			case ApplicationMNM.MSG_ID_ARTICLE_PARSER:	
				switch( errorID ) {
				case BaseRSSWorkerThread.ERROR_INVALID_RSS_DATA:
					resID = R.string.feed_invalid_data; 
					break;
				case BaseRSSWorkerThread.ERROR_NO_INPUT_STREAM_FILE_NOT_FOUND:
					resID = R.string.feed_cache_file_not_found; 
					break;
				case BaseRSSWorkerThread.ERROR_NO_INPUT_STREAM_UNKOWN_HOST:
					resID = R.string.feed_host_unavailable; 
					break;
				}
			break;
		}
		return getResources().getString(resID);
	}
	
	/**
	 * Returns the URL this feed points too
	 * @return String - FeedURL
	 */
	public String getFeedURL() {
		if ( mbIsLoadingCachedFeed )
		{
			return getSDCardCacheFilePath();
		}
		return mFeedURL;
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public String getTabActivityTag() {
		return "";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public int getIndicatorStringID() {
		return -1;
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return -1;
	}
	
	/**
	 * Setup all basic data our worker thread needs to work well
	 */
	protected void setupWorkerThread() {
		// Get the max number of items to be shown from our preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        int maxItems = -1;        
        try
        {
        	maxItems = Integer.parseInt(prefs.getString("pref_app_maxarticles", "-1"));
        }
        catch( Exception e)
        {
        	// Nothing to do here :P
        }  
		mRssThread.setupWorker( maxItems, mHandler, getTabActivityTag(), getFeedURL(), mSemaphore );
	}
	
	/**
	 * Will return the feed but taken from the cache
	 * @return
	 */
	public Feed getFeedFromCache() {
		String storageType = getStorageType();
        if ( storageType.compareTo("Internal") == 0 )
        {
        	return mDBHelper.getFeed(getIndicatorStringID());
        }
        else if ( storageType.compareTo("SDCard") == 0 )
        {
        	// Make SD-card caching
        	return getFeedFromCacheSDCard();
        }
        return null;
	}
	
	/**
	 * Will refresh the current feed
	 */
	public void refreshFeed( boolean bUseCache ) {		
		// Start thread if not started or not alive
		// If we are loading a cached feed to we are pause we can not start!
		if ( !mbIsLoadingCachedFeed && !mbIsPaused && 
				( mRssThread == null || !mRssThread.isAlive() ))
		{
			String Error = "";
			mbIsLoadingCachedFeed = bUseCache;
			
			RequestFeedTaskParams mTaskParams = new RequestFeedTaskParams();
			mTaskParams.mMaxItems = -1;
			mTaskParams.mItemClass = "com.dcg.rss.ArticleFeedItem";
			mTaskParams.mURL = mFeedURL;
			mTaskParams.mParserClass = "com.dcg.rss.FeedParser";
			mTaskParams.mFeedListener = this;
			mTaskParams.mFeedID = getIndicatorStringID();
			new RequestFeedTask(this).execute(mTaskParams);
			
			// Clear the current list adapter!
			setListAdapter(null);
			
			// Change empty text so that the user knows when it's all done
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.refreshing_lable);
			
			/*
			try {
				// Clear the current list adapter!
				setListAdapter(null);

				// Change empty text so that the user knows when it's all done
				TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
				emptyTextView.setText(bUseCache?R.string.refreshing_lable_cached:R.string.refreshing_lable);
				
				// Refresh from source or from cache
				if ( mbIsLoadingCachedFeed )
				{
					// Start cache process
					onRefreshCompleted(COMPLETE_SUCCESSFULL,null, getFeedFromCache(), "");
					
					// Cache loaded!
					mbIsLoadingCachedFeed = false;
				}
				else
				{
					// Start with our task!
					ApplicationMNM.logCat(TAG, "Staring worker thread (RSS)");
					mRssThread = (BaseRSSWorkerThread) Class.forName( mRssWorkerThreadClassName ).newInstance();
					
					// Give our child's a chance to setup the thread
					setupWorkerThread();
					mRssThread.start();
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Error = e.toString();
			}
			/**/
			if ( Error.length() > 0 ) onRefreshCompleted(COMPLETE_ERROR,null,null,Error);
		}
		else
		{
			onRefreshCompleted(COMPLETE_ERROR_THREAD_ALIVE, null, null, "");
		}
	}
	
	/**
	 * Called once we finished requesting the feed
	 */
	public void onFeedFinished(Integer resultCode, Feed feed) {
		if ( resultCode == ApplicationMNM.ERROR_SUCCESSFULL )
		{
			this.mFeed = feed;
			_updateFeedList();
		}
	}
	
	/**
	 * Will assign the current article list to our ListView
	 */
	private void _updateFeedList()
	{
		// Clear out list adapter
		setListAdapter(null);
		
		// We can only assign a new feed if we are not paused!
		if ( !mbIsPaused )		
			try {
				// Set the new adapter!		
				if ( this.mFeed != null )
				{
					ArticlesAdapter listAdapter = (ArticlesAdapter) Class.forName( getListAdapterClassName() ).newInstance();
					ApplicationMNM.logCat(TAG, "Created list adapter: "+listAdapter.getClass().toString());
					listAdapter.setupAdapter(this, this.mFeed);
					setListAdapter(listAdapter);
					
					// Set right item
					mListView.setSelection(mFirstVisiblePosition);
					
					// List adapter now able to get data!
					listAdapter.mInitialized = true;
				}
			} catch ( Exception e ) {
				onRefreshCompleted(COMPLETE_ERROR, null, null, e.toString());
			}
	}
	
	/**
	 * Returns the class name of the list adapter we should use
	 * @return
	 */
	public String getListAdapterClassName() {
		return "com.dcg.adapter.ArticlesAdapter";
	}
	
	/**
	 * Return storage type used
	 * @return
	 */
	public String getStorageType() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        return prefs.getString("pref_app_storage", "SDCard");
	}
	
	/**
	 * Called when we finished to refresh a thread
	 */
	private void onRefreshCompleted( int completeCode, Bundle data, Feed parsedFeed, String Error )
	{
		String ErrorMsg = "";
		boolean bShowToast = true;
		TextView emptyTextView = null;
		switch( completeCode )
		{
		case COMPLETE_SUCCESSFULL:
			// We finished successfully!!! Yeah!
			ApplicationMNM.logCat(TAG,"Completed!");
			
			emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
			
			this.mFeed = parsedFeed;
			if ( this.mFeed != null )
			{
				this.mFeed.setIdentification(getIndicatorStringID(),getFeedURL());
				
				// If no articles where found in the feed show an advice
				if ( this.mFeed.getArticleCount() == 0 )
				{
					ApplicationMNM.showToast(R.string.feed_no_articles);
					// Do not save an empty feed :P
					this.mFeed = null;
				}
				else
				{				
					// If we are loading a cached feed do not cache it again
					if ( !mbIsLoadingCachedFeed )
					{
						// Start caching process
						String storageType = getStorageType();
				        if ( storageType.compareTo("Internal") == 0 )
				        {
				        	saveFeedIntoDB(true);
				        }
				        else if ( storageType.compareTo("SDCard") == 0 )
				        {
				        	// Make SD-card caching
				        	startSDCardCaching();
				        }
					}
					// Update feed
					_updateFeedList();
				}
			}
			break;
		case COMPLETE_ERROR_THREAD_ALIVE:
			bShowToast = false;
			ErrorMsg = getResources().getString(R.string.refreshing_thread_still_alive);
			break;
		case COMPLETE_ERROR:
			ErrorMsg = getResources().getString(R.string.refreshing_failed)+" "+Error;
			// Change empty text so that the user knows when it's all done
			emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
			break;
		}
		if ( !ErrorMsg.equals("") )
		{
			ApplicationMNM.logCat(TAG, ErrorMsg);
			if ( bShowToast )
			{
				ApplicationMNM.showToast(ErrorMsg);
			}
		}
		
		// Clear references out
		mRssThread = null;
		mbIsLoadingCachedFeed = false;
	}
	
	/* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_REFRESH, 0, R.string.main_menu_refresh).setIcon(R.drawable.ic_menu_refresh);
        menu.add(1, MENU_NOTAME, 0, R.string.main_menu_notame).setIcon(android.R.drawable.ic_menu_send);
    	menu.add(1, MENU_SETTINGS, 0, R.string.main_menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
    	menu.add(1, MENU_ABOUT, 0, R.string.main_menu_about).setIcon(android.R.drawable.ic_menu_info_details);
    	return true;
    }
    
    /** */
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupEnabled(0, (mRssThread == null || !mRssThread.isAlive()));
    	return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) 
        {
        case MENU_REFRESH:
            // Refresh !   	
        	refreshFeed( false );
            return true;
        case MENU_NOTAME:
        	// Open notame activity
        	openNotameScreen();
        	return true;
        case MENU_SETTINGS:
            // Open settitngs screen
        	openSettingsScreen();
            return true;
        case MENU_ABOUT:
        	AboutDialog aboutDialog = new AboutDialog(this);
        	aboutDialog.show();
        	return true;
        }
        return false;
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
    	if ( mListView != null )
    	{
    		FeedItem selecteItem = (FeedItem)mListView.getAdapter().getItem(menuInfo.position);
    		// Get the real item
    		if ( selecteItem != null )
    		{
	    		switch (item.getItemId()) 
	    		{
		    	case CONTEXT_MENU_OPEN:
		        case CONTEXT_MENU_OPEN_SOURCE:
		    			String url = "";
		    			if (item.getItemId() == CONTEXT_MENU_OPEN)
		    			{
		    				url = (String)selecteItem.getKeyData("link");
		    				ApplicationMNM.showToast(getResources().getString(R.string.context_menu_open));
		    			}
		    			else
		    			{
		    				url = (String)selecteItem.getKeyData("url");
		    				ApplicationMNM.showToast(getResources().getString(R.string.context_menu_open_source));
		    			}
		    			try
		    			{
		    				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
		    			} catch ( Exception e )
		    			{
		    				ApplicationMNM.warnCat(TAG, "Can not open URI in browser: " + e.toString());
		    			}
		    		
		        	return true;
		    	case CONTEXT_MENU_VOTE:
		    		new MenealoTask(this).execute(selecteItem.getArticleID());
		    		//menealo(selecteItem);
		        	return true;
		        }
    		}
    		else
    		{
    			ApplicationMNM.warnCat(TAG,"List item null or not a FeedItem");
    		}
    	}
    	else
    	{
    		ApplicationMNM.warnCat(TAG,"No ListView found in layout for " + this.toString());
    	}
    	return false;
    }
    
    /**
     * Open settings screen 
     */
    public void openSettingsScreen() {
    	Intent settingsActivity = new Intent( this, Preferences.class);
    	startActivityForResult(settingsActivity, SUB_ACT_SETTINGS_ID);
    	
    	// TODO: Catch result!
    }
    
    /**
     * Open notame activity
     */
    public void openNotameScreen() {
    	if ( hasNotameDataSetup() )
    	{
    		Intent notameActivity = new Intent( this, NotameActivity.class);
    		startActivityForResult(notameActivity, SUB_ACT_NOTAME_ID);
    	}
    	else
    	{
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.notame_setup_data)
				.setCancelable(false)
				.setTitle(R.string.notame_setup_data_tilte)
				.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						openSettingsScreen();
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.generic_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			AlertDialog openSettingsDialog = builder.create();
			openSettingsDialog.show();
    	}
    }
    
    /**
     * Did the user set the needed notame data or not?
     * @return
     */
    public boolean hasNotameDataSetup() {
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
		String userName = prefs.getString("pref_account_user", "");
		String APIKey = prefs.getString("pref_account_apikey", "");
		return userName.compareTo("") != 0 && APIKey.compareTo("") != 0;
    }
    
    /**
     * Return a feed from the SD Card cache
     * @return
     */
    public Feed getFeedFromCacheSDCard() {
    	Feed feed = null;
    	try {
    		/*
    		feed = new Feed();
    		FileInputStream feedFile = new FileInputStream(getSDCardCacheFilePath());
    		DataInputStream fileStream = new DataInputStream(feedFile);
    		BufferedReader bufferReader = new BufferedReader(new InputStreamReader(fileStream));
    		String strLine;
    		// The definition file has 7 lines!
    		int lineNumber = 0;
    		String feedID = "";
    		while((strLine = bufferReader.readLine()) != null)
    		{
    			if ( lineNumber < 7 )
    			{
    				// Parse item depending on line
    				switch(lineNumber) {
    					case 0:
    						feedID = strLine.trim();
    						break;
    					case 1:
    						feed.setIdentification(getIndicatorStringID(), strLine.trim());
    						break;
    				}
    			}
    			else
    			{
    				break;
    			}
    			lineNumber++;
    		}
    		fileStream.close();
    		/**/
    	} catch( Exception e ) {
    		// nothing to be done
    	}
    	return feed;
    }
    
    /**
     * Starts the internal caching process to the SD-card
     */
    private void startSDCardCaching() {
    	// Start creating the cache folders
    	if ( prepareSDCard() )
    	{
    		FileWriter feedFile = null;
    		try {
    			// Create feed definition file
    			feedFile = new FileWriter( getSDCardCacheFilePath() );    			
    			if (feedFile != null)
				{
    				// Order is important!
    				feedFile.write(mFeed.getFeedID() + "\n");
    				feedFile.write(mFeed.getURL() + "\n");
    				feedFile.write(mFeed.getFirstVisiblePosition() + "\n");
    				feedFile.write(mFeed.getArticleCount() + "\n");
    				feedFile.write(mFeed.getRawKeyData("title") + "\n");
    				feedFile.write(mFeed.getRawKeyData("description") + "\n");
    				feedFile.write(mFeed.getRawKeyData("url")); 
    				
    				// Close file
					feedFile.close();
				}
    			
    			// Create article files
    			List<FeedItem> articles =  mFeed.getArticleList();
                int articlesNum = articles.size();
                for ( int i = 0; i < articlesNum; i++ )
                {
                	FeedItem item = articles.get(i);
                	if ( item != null )
                	{
	                	feedFile = new FileWriter( getSDCardCacheItemFilePath(i) );   
	                	if (feedFile != null)
	                	{
	                		// Order is important
	                		feedFile.write(i + "\n");
	                		feedFile.write(item.getRawKeyData("title") + "\n");
	                		feedFile.write(item.getRawKeyData("description") + "\n");
	                		feedFile.write(item.getRawKeyData("votes") + "\n");
	                		feedFile.write(item.getRawKeyData("url") + "\n");
	                		feedFile.write(item.getRawKeyData("category") + "\n");
	                		feedFile.write(item.getRawKeyData("link") + "\n");
	                		feedFile.write(item.getRawKeyData("commentRss") + "\n");
	                		feedFile.write(item.getRawKeyData("link_id"));
	                		
	                		// Close file
	    					feedFile.close();
	                	}
                	}
                }
    			
    			ApplicationMNM.logCat(TAG, "Feed written to: "+getSDCardCacheFolderPath()+File.separator);
    			// Caching finished so delete article references, we need to save memory!
    			mFeed.clearArticleList();
    		} catch (Exception e) {
				ApplicationMNM.showToast("Failed to save feed to SD-Card");
				e.printStackTrace();
			} finally {
				try {
					if (feedFile != null)
					{
						feedFile.close();
					}
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
    	}
    	else
    	{
    		ApplicationMNM.warnCat(TAG, "Can not cache to SD-Card!");
    	}
    }
    
    /** 
     * Returns the folder we will use to cache the feed to the SD-Card
     * */
    private String getSDCardCacheFolderPath() {
    	return ApplicationMNM.getRootCacheFolder()+getTabActivityTag();
    }
    
    /** 
     * Returns the path to the feed cache file in the SD-Card
     * */
    private String getSDCardCacheFilePath() {
    	return getSDCardCacheFolderPath()+File.separator+"feed.definition";
    }
    
    /**
     * Get the path to cache file of a feed item
     * @param itemID
     * @return
     */
    private String getSDCardCacheItemFilePath( int itemID ) {
    	return getSDCardCacheFolderPath()+File.separator+"feed."+itemID+".item";
    }
    
    /**
     * Look if we have a cache file or not
     * @return
     */
    public boolean hasCachedFeed( String storageType ) {
    	if ( storageType.compareTo("SDCard") == 0 )
    	{
    		File file = new File(getSDCardCacheFilePath());
    		return file.exists();
    	}
    	else
    	{
    		// We always try to access the feed from the database! (is this ok?)
    		return true;
    	}
    }
    
    /**
	 * Prepares the SDCard with all we need for the caching process
	 */
	protected boolean prepareSDCard() {
		try {
			// Create app dir in SDCard if possible
			File path = new File( getSDCardCacheFolderPath() );
			try {
				// Before we create the directory we purge it's content!
				ApplicationMNM.fileDelete( path );
			} catch( Exception e ) {
				// Nothing to be done!
			}
			if(!path.isDirectory()) {
				if ( path.mkdirs() )
				{
					ApplicationMNM.logCat(TAG,"Directory created: " + path);
				}
				else
				{
					ApplicationMNM.warnCat(TAG,"Failed to create directory: " + path);
				}
			}			
			return true;
		} catch( Exception e )
		{
			ApplicationMNM.warnCat(TAG,"Failed to prepare SD card for aching: " + e.toString());
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * Will vote an article/comment
	 */
	/*
	public void menealo( FeedItem item ) {
		if ( hasMenealoDataSetup() )
		{
			String menealoURL = buildMenealoURL();
		}
	}
	/**/
	
	/**
     * Did the user set the needed notame data or not?
     * @return
     */
	/*
    
    /**/
	
	/**
	 * Will retunr the full menealo url used send votes for articles
	 * @return
	 */
    /*
	private String buildMenealoURL() {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
			int userID = Integer.parseInt(prefs.getString("pref_account_user_id", "0"));
		} catch( Exception e ) {
			ApplicationMNM.showToast(R.string.user_id_invalid);
		}
		return "";
	}
	/**/
	
	/**
	 * Thread responsible to vote an articel and respond :P
	 * @author Moritz Wundke (b.thax.dcg@gmail.com)
	 */
	/*
	public class MenealoThread extends Thread {
		private HttpClient mHttpClient = null;
		private String mURL = "";
		
		public void setupThread( HttpClient HttpClient, String URL ) {
			mHttpClient = HttpClient;
			mURL = URL;
		}
		
	    public void run() {
	    	boolean bResult = false;
	    	ApplicationMNM.logCat(TAG, "Sending n�tame message");
	    	
	    	if ( mHttpClient != null )
	    	{
	    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
    			String userName = prefs.getString("pref_account_user", "");
    			String userID = prefs.getString("pref_account_user_id", "0");
    			String APIKey = prefs.getString("pref_account_apikey", "");
	    		
	    		String URL = ApplicationMNM.MENEAME_BASE_URL + MENEAME_MENEALO_API + "?user=" + userName + "&key=" + APIKey + "&charset=utf-8";
	    		HttpGet httpGet = new HttpGet(URL);
	    		try {
	    			
	    			// Execute
	    			HttpResponse response = mHttpClient.execute(httpGet);
	    			if ( response != null )
	    			{
	    				InputStreamReader inputStream = new InputStreamReader(response.getEntity().getContent());
	    				int data = inputStream.read();
	    				String finalData = "";
	    				while(data != -1){
	    					finalData += (char) data;
	    					data = inputStream.read();
	    				}
	    				inputStream.close();
	    				Log.d(TAG,finalData);
	    				// Did we got an ok?
	    				bResult = finalData.startsWith("OK");
	    			}
	    		} catch ( Exception e) {
	    			// Nothing to be done
	    		}
	    	}
	    	
	    	if ( mHandler != null )
	    	{
	    		Message msg = mHandler.obtainMessage();
	    		Bundle data = new Bundle();
	    		data.putBoolean(MENEALO_RESULT_KEY, bResult);
	    		data.putInt(ApplicationMNM.MSG_ID_KEY, ApplicationMNM.MSG_ID_MENEALO);
	    		msg.setData(data);
	    		mHandler.sendMessage(msg);
	    	}	    	
	    }
	}
	/**/
}
