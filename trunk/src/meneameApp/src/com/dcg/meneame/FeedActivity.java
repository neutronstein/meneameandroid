package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.Toast;

abstract public class FeedActivity extends ListActivity {
	
	/** Global Application */
	protected ApplicationMNM mApp = null;
	
	/** Feed URL */
	protected String mFeedURL = "";
	
	/** Semaphore used by the activities feed worker thread */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** Worker thread which will do the async operations */
	private RssWorkerThread mRssThread = null;
	
	public FeedActivity() {
		super();
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
	 * Will refresh the current feed
	 */
	public void RefreshFeed() {
		Toast toast = Toast.makeText(getBaseContext(), "Refreshing: " + getFeedURL(), Toast.LENGTH_SHORT);
		toast.show();
		
		// Start thread, normally we should build a handler and so
		mRssThread = new RssWorkerThread(mApp, getFeedURL(), mSemaphore);
		mRssThread.start();
	}
}
