package com.dcg.meneame;

import java.io.File;
import java.util.concurrent.Semaphore;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.dcg.adapter.FeedItemAdapter;
import com.dcg.adapter.FeedItemViewHolder;
import com.dcg.app.ApplicationMNM;
import com.dcg.dialog.AboutDialog;
import com.dcg.provider.FeedItemElement;
import com.dcg.task.MenealoTask;
import com.dcg.task.RequestFeedTask;
import com.dcg.task.RequestFeedTaskParams;
import com.dcg.task.RequestFeedTask.RequestFeedListener;

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
	
	/** Our cached main list view */
	private ListView mListView = null;
	
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
    
    /** Used to debug, will print all article ID for this feed tab into the log */
    public static final boolean mbPrintArticleIDsOnStart = false;
    
    /** Is this an article or an comments feed? */
    protected boolean mbIsArticleFeed;
    
    /** Are we paused or not? */
    protected boolean mbIsPaused;
    
    /** Are we loading a cached feed? */
    protected boolean mbIsLoadingCachedFeed;
    
    /** Last visible item when we entered the pause state */
    private int mFirstVisiblePosition= -1;

    /** Request a feed from the meneame server */
    private RequestFeedTask mRequestFeedTask = null;
    
    public FeedActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);		
		mbIsArticleFeed = true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onCreate()");
		
		if ( mbPrintArticleIDsOnStart )
		{
			// Form an array specifying which columns to return. 
			String[] projection = new String[] {
					FeedItemElement._ID,
					FeedItemElement.LINK_ID
					};
			
			final String[] arguments1 = new String[1];
			arguments1[0] = String.valueOf(getIndicatorStringID());
			final String where = FeedItemElement.FEEDID + "=?";
			
			// Make the query.
			Cursor cur = managedQuery(FeedItemElement.CONTENT_URI, projection, where, arguments1, null);
			
			// Print all articles we got out!
			if ( cur != null && cur.moveToFirst() )
			{
				int rowID = 0;
				do {
					ApplicationMNM.logCat(TAG, " ["+rowID+"]FeedItem: "+cur.getString(cur.getColumnIndex(FeedItemElement.LINK_ID)));
					rowID++;
				} while (cur.moveToNext() );
			}
			
			// Once we are finished close the cursor
			if ( cur != null )
			{
				cur.close();
			}
		}
		
		// Unpause
		mbIsPaused = false;
		
		// Perpare layout
		setContentView(R.layout.meneo_list);
		
		// Do final stuff
		setupViews();
		
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
		
		// Restore app state if any
		restoreState();
		
		// Unpause
		mbIsPaused = false;
		
		// Set empty list text		
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		emptyTextView.setText(R.string.empty_list);
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
		
		TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
		emptyTextView.setText("");
		
		// Pause
		mbIsPaused = true;
		
		// Free listadapter
		//setListAdapter(null);
		
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
		if (mRequestFeedTask != null)
			mRequestFeedTask.cancel(true);
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
		try {
			ApplicationMNM.logCat(TAG, " - First visible position: " + mListView.getFirstVisiblePosition());
			// Save state
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
			// Restore state
		} catch( Exception e) {
			ApplicationMNM.warnCat(TAG, "Failed to restore app state: "+e.toString());
		}
	}
	/**
	 * IF we touch the screen and we do not have any feed and no request has been
	 * made refresh the feed from the net
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		ApplicationMNM.logCat(TAG, getTabActivityTag()+"::onTouchEvent()");
		// If the users touches the screen and no feed is setup refresh it!
		// TODO: check list view items
		if ( !mbIsPaused && mRequestFeedTask == null )
		{
			refreshFeed( false );
		}
		return super.onTouchEvent(event);
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
    	if ( shouldRefreshOnLaunch() )
        {
        	refreshFeed( false );
        }
	}
	
	/**
	 * Set a cursor adapter for our list
	 */
	protected void setCursorAdapter() {
		// Use: setFilterText(queryString); to set the filter.
		mListView.setAdapter(new FeedItemAdapter(this, FeedItemElement.FEEDID+"=?",new String[]{String.valueOf(getFeedID())}));
	}
	
	/**
	 * Setup view
	 */
	protected void setupViews() {
		mListView = getListView();
		
		if ( mListView != null )
		{
			// Set adapter
			setCursorAdapter();
			
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
	 * Return the ID used for this feed tab
	 */
	public int getFeedID() {
		return getIndicatorStringID();
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
	 * Will refresh the current feed
	 */
	public void refreshFeed( boolean bUseCache ) {		
		// Start thread if not started or not alive
		// If we are loading a cached feed to we are pause we can not start!
		if ( !mbIsPaused &&  mRequestFeedTask == null )
		{			
			mbIsLoadingCachedFeed = bUseCache;
			
			RequestFeedTaskParams mTaskParams = new RequestFeedTaskParams();
			mTaskParams.mMaxItems = -1;
			mTaskParams.mItemClass = "com.dcg.rss.ArticleFeedItem";
			mTaskParams.mURL = mFeedURL;
			mTaskParams.mParserClass = "com.dcg.rss.FeedParser";
			mTaskParams.mFeedListener = this;
			mTaskParams.mFeedID = getIndicatorStringID();
			// Create task and run it
			mRequestFeedTask = new RequestFeedTask(this);
			mRequestFeedTask.execute(mTaskParams);
			
			// Clear the current list adapter!
			setListAdapter(null);
			
			// Change empty text so that the user knows when it's all done
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.refreshing_lable);
		}
		else
		{
			// Mhh already a feed active :P
		}
	}
	
	/**
	 * Called once we finished requesting the feed
	 */
	public void onFeedFinished(Integer resultCode) {
		if ( resultCode == ApplicationMNM.ERROR_SUCCESSFULL )
		{	
			// Null task
			mRequestFeedTask = null;
			
			// Set the cursor adapter
			setCursorAdapter();
			
			// Set empty list text		
			TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
			emptyTextView.setText(R.string.empty_list);
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
    	menu.setGroupEnabled(0, mRequestFeedTask == null);
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
    		View view = (View)mListView.getAdapter().getItem(menuInfo.position);
    		// Get the real item
    		if ( view != null )
    		{
        		FeedItemViewHolder viewTag = (FeedItemViewHolder)view.getTag();
	    		switch (item.getItemId()) 
	    		{
		    	case CONTEXT_MENU_OPEN:
		        case CONTEXT_MENU_OPEN_SOURCE:
		    			String url = "";
		    			if (item.getItemId() == CONTEXT_MENU_OPEN)
		    			{
		    				url = viewTag.link;
		    				ApplicationMNM.showToast(getResources().getString(R.string.context_menu_open));
		    			}
		    			else
		    			{
		    				url = (String)viewTag.url.getText();
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
		    		new MenealoTask(this).execute(viewTag.link_id);
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
}
