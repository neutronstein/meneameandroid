package com.dcg.meneame;

import java.io.File;
import java.util.concurrent.Semaphore;

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
	protected static final String mRssWorkerThreadClassName = "com.dcg.meneame.RSSWorkerThread";

	/** Global Application */
	protected ApplicationMNM mApp = null;
	
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
		
		 // Cache app
		try {
			mApp = (ApplicationMNM)getApplication();
		} catch(Exception e){
			e.printStackTrace();
		}
		
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
		
		// Recover the feed if that was possible
		try
		{
			mFeed = (Feed)savedInstanceState.getParcelable(getTabActivityTag());
		} catch (Exception e)
		{
			// Nothing needs to be done here
		}
		
		// Refresh if needed
		_conditionRefreshFeed();
	}
	
	/**
	 * Save state data into
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(getTabActivityTag(), mFeed);
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// If the users touches the screen and no feed is setup refresh it!
		if ( mFeed == null && (mRssThread == null || !mRssThread.isAlive()) )
		{
			refreshFeed();
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
	        	refreshFeed();
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
		mRssThread.setupWorker( mApp, mHandler, getFeedURL(), mSemaphore );
	}
	
	/**
	 * Will refresh the current feed but taken the data from the cache
	 */
	public void buildFromCache() {
		
	}
	
	/**
	 * Will refresh the current feed
	 */
	public void refreshFeed() {		
		// Start thread if not started or not alive
		if ( mRssThread == null || !mRssThread.isAlive() )
		{
			String Error = "";
			try {
				// Clear the current list adapter!
				setListAdapter(null);

				// Change empty text so that the user knows when it's all done
				TextView emptyTextView = (TextView) findViewById(android.R.id.empty);
				emptyTextView.setText(R.string.refreshing_lable);
				
				// Start with our task!
				ApplicationMNM.logCat(TAG, "Staring worker thread");
				mRssThread = (BaseRSSWorkerThread) Class.forName( mRssWorkerThreadClassName ).newInstance();
				
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
		// Clear out list adapter
		setListAdapter(null);
		
		// Set the new adapter!		
		if ( this.mFeed != null )
		{
			setListAdapter(new ArticlesAdapter(this, this.mFeed));
		}
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
			
			// Start caching process
			// TODO: Put all this into another thread process!
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
	        String mainAnim = prefs.getString("pref_app_storage", "Internal");
	        if ( mainAnim.compareTo("Internal") == 0 )
	        {
	        	// Make DB caching
	        	ApplicationMNM.showToast(R.string.advice_not_implemented);
	        }
	        else if ( mainAnim.compareTo("SDCard") == 0 )
	        {
	        	// Make SD-card caching
	        	startInternalCaching();
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
		
		// Clear refernce out
		mRssThread = null;
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
        	refreshFeed();
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
		    		menealo(selecteItem);
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
    private void startInternalCaching() {
    	// Start creating the cache folders
    	if ( prepareSDCard() )
    	{
    		// Cache!
    	}
    	else
    	{
    		ApplicationMNM.showToast("Can not cache to SD-Card!");
    	}
    }
    
    /**
	 * Prepares the SDCard with all we need for the caching process
	 */
	protected boolean prepareSDCard() {
		try {			
			// Create app dir in SDCard if possible
			File path = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"com.dcg.meneame"+File.separator+"cache"+File.separator+getTabActivityTag());
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
