package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.BaseRSSWorkerThread;
import com.dcg.util.rss.Feed;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
	
	/** Handler used to communicate with our worker thread*/
	protected Handler mHandler = null;
	
	/** Codes used to inform our activity how we completed */
	public static final int COMPLETE_SUCCESSFULL = 0;
	public static final int COMPLETE_ERROR_THREAD_ALIVE = 1;
	public static final int COMPLETE_ERROR = 2;
	
	public FeedActivity() {
		super();
		
		ApplicationMNM.AddLogCat(TAG);
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
				errorMsg = "msg.obj is null!";
				if ( msg.obj != null )
				{
					errorMsg = "msg.obj is not a Feed object "+ msg.obj.toString();
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
				errorMsg = "Unkown!";
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
	public static String getTabActivityTag() {
		return "";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int getIndicatorStringID() {
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
				ApplicationMNM.LogCat(TAG, "Staring worker thread");
				ApplicationMNM.showToast("Refreshing: " + getFeedURL());
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
			}
			if ( Error.length() > 0 ) onRefreshCompleted(COMPLETE_ERROR,null,null,Error);
		}
		else
		{
			onRefreshCompleted(COMPLETE_ERROR_THREAD_ALIVE, null, null, "");
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
			ApplicationMNM.LogCat(TAG,"Completed!");
			ApplicationMNM.showToast("Completed!");
			
			// Set the new adapter!
			
			setListAdapter(new ArticlesAdapter(this, parsedFeed.getArticleList()));
			break;
		case COMPLETE_ERROR_THREAD_ALIVE:
			ErrorMsg = "Worker thread still alive!";
			break;
		case COMPLETE_ERROR:
			ErrorMsg = "Failed to refresh feed: "+Error;
			break;
		}
		if ( !ErrorMsg.equals("") )
		{
			ApplicationMNM.LogCat(TAG, ErrorMsg);
			ApplicationMNM.showToast(ErrorMsg);
		}
	}
}
