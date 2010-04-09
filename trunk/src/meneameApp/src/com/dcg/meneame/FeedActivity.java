package com.dcg.meneame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.xmlpull.v1.XmlSerializer;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.BaseRSSWorkerThread;
import com.dcg.util.rss.Feed;
import com.dcg.util.rss.FeedItem;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Xml;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Basic activity that handles feed parsing and stuff like that
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
abstract public class FeedActivity extends ListActivity {
	
	/** Log tag */
	private static final String TAG = "FeedActivity";
	
	/** Our RssWorkerThread class so subclasses will be able to call another one */
	protected static String mRssWorkerThreadClassName = "com.dcg.meneame.RSSWorkerThread";
	protected static String mLocalRssWorkerThreadClassName = "com.dcg.meneame.LocalRSSWorkerThread";
	
	/** Class used for our list adapters */
	protected static String mListAdapterClass = "com.dcg.meneame.ArticlesAdapter";
	
	/** Feed URL */
	protected String mFeedURL = "";
	
	/** Semaphore used by the activities feed worker thread */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** Worker thread which will do the async operations */
	private BaseRSSWorkerThread mRssThread = null;
	
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
    
    /** Settings activity result ID */
    private static final int SUB_ACT_SETTINGS_ID = 0;
    
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
    
    /** The following constants will define all basic URL's meneame will handle */
    private static final String MENEAME_BASE_URL = "http://www.meneame.net";
    private static final String MENEAME_MENEALO_API = "/backend/menealo.php";
    private static final String MENEAME_MENEALO_COMMENT_API = "/backend/menealo_comment.php";
    
	public FeedActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);		
		mbIsArticleFeed = true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onCreate()");
		
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
		
		// Make sure or empty list text is set to it's default
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		emptyTextView.setText(R.string.empty_list);
		
		// Look if we need to request a stop for the current caching thread
		if ( mbIsLoadingCachedFeed && mRssThread != null )
		{
			mRssThread.requestStop();
			mRssThread = null;
			mbIsLoadingCachedFeed = false;
		}
		
		// Should be get the feed from the cached file?
		if ( mFeed == null && (mRssThread == null || !mRssThread.isAlive()) )
		{
			// If the refresh thread is active kill it in case it's a cached one
			String storageType = getStorageType();
	        if ( storageType.compareTo("SDCard") == 0 )
	        {
	        	try {
					//InputStreamReader reader = new InputStreamReader(new FileInputStream ( this.getSDCardCacheFilePath()), "UTF-8");
	        		refreshFeed( true );
	        	} catch (Exception e) {
					// Not cached
				}
	        }
		}
		else
		{
			// We got a saved thread so set it again!
			_updateFeedList();
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
		
		// Look if we need to request a stop for the current caching thread
		if ( mbIsLoadingCachedFeed && mRssThread != null )
		{
			mRssThread.requestStop();
			mRssThread = null;
			mbIsLoadingCachedFeed = false;
		}
		
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
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onSaveInstanceState()");
		/*
		if ( mFeed != null )
		{
			ApplicationMNM.logCat(TAG, " saving feed...");
			outState.putParcelable("currentlySavedFeed", mFeed);
		}
		*/
		super.onSaveInstanceState(outState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle state) {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onRestoreInstanceState()");
		/*
		mFeed = state.getParcelable("currentlySavedFeed");
		*/
		super.onRestoreInstanceState(state);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onTouchEvent()");
		// If the users touches the screen and no feed is setup refresh it!
		if ( mFeed == null && (mRssThread == null || !mRssThread.isAlive()) )
		{
			refreshFeed( false );
		}
		return super.onTouchEvent(event);
	}
	
	private void _conditionRefreshFeed() {
		if ( mFeed != null )
		{
			// We got already a feed, so just set a new adapter
			_updateFeedList();
		}
		else
		{
	    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
	        if ( prefs.getBoolean("pref_app_refreshonlaunch", false) )
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
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::handleThreadMessage()");
		
		String errorMsg = "";
		// Check if it completed ok or not
		if ( data.getInt( BaseRSSWorkerThread.COMPLETED_KEY) == BaseRSSWorkerThread.COMPLETED_OK )
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
				if ( !errorMsg.equals("") )
				{
					onRefreshCompleted(COMPLETE_ERROR, null, null, errorMsg);
				}
			}
		}
		else
		{
			if ( data.getInt(BaseRSSWorkerThread.ERROR_KEY) == BaseRSSWorkerThread.ERROR_FAILED )
			{
				errorMsg = data.getString(BaseRSSWorkerThread.ERROR_MESSAGE_KEY);
			}
			else
			{
				errorMsg = getResources().getString(R.string.general_unknown);
			}
			onRefreshCompleted(COMPLETE_ERROR, null, null, errorMsg);
		}
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
		mRssThread.setupWorker( (ApplicationMNM)getApplication(), mHandler, getFeedURL(), mSemaphore );
	}
	
	/**
	 * Will refresh the current feed but taken the data from the cache
	 */
	public void buildFromCache() {
		
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
			try {
				// Clear the current list adapter!
				setListAdapter(null);

				// Change empty text so that the user knows when it's all done
				TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
				emptyTextView.setText(R.string.refreshing_lable);
				
				// Start with our task!
				ApplicationMNM.logCat(TAG, "Staring worker thread");
				String threadClassName = bUseCache?mLocalRssWorkerThreadClassName:mRssWorkerThreadClassName;
				mRssThread = (BaseRSSWorkerThread) Class.forName( threadClassName ).newInstance();
				
				// Give our child's a chance to setup the thread
				setupWorkerThread();
				mRssThread.start();
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
			if ( Error.length() > 0 ) onRefreshCompleted(COMPLETE_ERROR,null,null,Error);
		}
		else
		{
			onRefreshCompleted(COMPLETE_ERROR_THREAD_ALIVE, null, null, "");
		}
	}
	
	/**
	 * Will assign the current article list to our ListView
	 */
	private void _updateFeedList()
	{
		// TODO: Need a methos to update just the adapter to add single items!
		// http://www.softwarepassion.com/android-series-custom-listview-items-and-adapters/
		
		// Clear out list adapter
		setListAdapter(null);
		
		// We can only assign a new feed if we are not paused!
		if ( !mbIsPaused )		
			try {
				// Set the new adapter!		
				if ( this.mFeed != null )
				{
					ArticlesAdapter listAdapter = (ArticlesAdapter) Class.forName( mListAdapterClass ).newInstance();
					listAdapter.setupAdapter(this, this.mFeed);
					setListAdapter(listAdapter);
				}
			} catch ( Exception e ) {
				onRefreshCompleted(COMPLETE_ERROR, null, null, e.toString());
			}
	}
	
	/**
	 * Return storage type used
	 * @return
	 */
	public String getStorageType() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        return prefs.getString("pref_app_storage", "Internal");
	}
	
	/**
	 * Called when we finished to refresh a thread
	 */
	private void onRefreshCompleted( int completeCode, Bundle data, Feed parsedFeed, String Error )
	{
		String ErrorMsg = "";
		switch( completeCode )
		{
		case COMPLETE_SUCCESSFULL:
			// We finished successfully!!! Yeah!
			ApplicationMNM.logCat(TAG,"Completed!");
			this.mFeed = parsedFeed;
			
			// If we are loading a cached feed do not cache it again
			if ( !mbIsLoadingCachedFeed )
			{
				// Start caching process
				String storageType = getStorageType();
		        if ( storageType.compareTo("Internal") == 0 )
		        {
		        	// Make DB caching
		        	ApplicationMNM.showToast(R.string.advice_not_implemented);
		        }
		        else if ( storageType.compareTo("SDCard") == 0 )
		        {
		        	// Make SD-card caching
		        	startSDCardCaching( parsedFeed );
		        }
			}
			// Update feed
			_updateFeedList();
			break;
		case COMPLETE_ERROR_THREAD_ALIVE:
			ErrorMsg = getResources().getString(R.string.refreshing_thread_still_alive);
			break;
		case COMPLETE_ERROR:
			ErrorMsg = getResources().getString(R.string.refreshing_failed)+" "+Error;
			// Change empty text so that the user knows when it's all done
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
			break;
		}
		if ( !ErrorMsg.equals("") )
		{
			ApplicationMNM.logCat(TAG, ErrorMsg);
			ApplicationMNM.showToast(ErrorMsg);
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
        	ApplicationMNM.showToast(R.string.advice_not_implemented);
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
		    		ApplicationMNM.showToast(R.string.advice_not_implemented);
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
    
    /* Open settings screen */
    public void openSettingsScreen() {
    	Intent settingsActivity = new Intent( this, Preferences.class);
    	startActivityForResult(settingsActivity, SUB_ACT_SETTINGS_ID);
    	
    	// TODO: Catch result!
    }
    
    /**
     * Starts the internal caching process to the SD-card
     */
    private void startSDCardCaching( Feed feed ) {
    	// Start creating the cache folders
    	if ( prepareSDCard() )
    	{
    		FileWriter feedFile = null;
    		try {
	    		// Create the xml content
	    		String feedXML = createXMLFeed( feed );
	    		
	    		// Save it to disc
	    		feedFile = new FileWriter( getSDCardCacheFilePath() );	    		
	    		feedFile.write(feedXML);
	    		ApplicationMNM.showToast("Feed written to: "+getSDCardCacheFilePath());
	    		ApplicationMNM.logCat(TAG, "Feed written to: "+getSDCardCacheFilePath());
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
    		ApplicationMNM.showToast("Can not cache to SD-Card!");
    	}
    }
    
    /**
     * Create an xml file for the current feed
     * @param feed
     * @return
     */
    private String createXMLFeed( Feed feed ){
        XmlSerializer serializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();
        try {
        	// Create document       	
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            
            // Start channel
            serializer.startTag("", "channel");
            
            // Add all feed data
            Map<String, String> feedData = feed.getAllData();
            for (String s: feedData.keySet()) {
            	serializer.startTag("", s);
            	serializer.text(feedData.get(s));
            	serializer.endTag("", s);
            }
            
            // Add all items
            List<FeedItem> articles =  feed.getArticleList();
            int articlesNum = articles.size();
            for ( int i = 0; i < articlesNum; i++ )
            {
            	FeedItem item = articles.get(i);
            	if ( item != null )
            	{
	            	serializer.startTag("", "item");
	            	
	            	// Add item data
	            	Map<String, String> itemData = item.getAllData();
	                for (String s: itemData.keySet()) {
	                	serializer.startTag("", s);
	                	serializer.text(itemData.get(s));
	                	serializer.endTag("", s);
	                }
	            	serializer.endTag("", "item");
            	}
            }
            serializer.endTag("", "channel");
            serializer.endDocument();
            return writer.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
    
    /** 
     * Returns the folder we will use to cache the feed to the SD-Card
     * */
    private String getSDCardCacheFolderPath() {
    	return Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"com.dcg.meneame"+File.separator+"cache"+File.separator+getTabActivityTag();
    }
    
    /** 
     * Returns the path to the feed cache file in the SD-Card
     * */
    private String getSDCardCacheFilePath() {
    	return getSDCardCacheFolderPath()+File.separator+"feed.rss";
    }
    
    /**
	 * Prepares the SDCard with all we need for the caching process
	 */
	protected boolean prepareSDCard() {
		try {			
			// Create app dir in SDCard if possible
			File path = new File( getSDCardCacheFolderPath() );
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
	public void menealo( FeedItem item ) {
		String menealoURL = buildMenealoURL();
	}
	
	/**
	 * Will retunr the full menealo url used send votes for articles
	 * @return
	 */
	private String buildMenealoURL() {
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
			int userID = Integer.parseInt(prefs.getString("pref_account_user_id", "0"));
		} catch( Exception e ) {
			ApplicationMNM.showToast(R.string.user_id_invalid);
		}
		return "";
	}
}
