package com.dcg.meneame;


import android.os.Bundle;

public class NewsActivity extends FeedActivity {
	
	public NewsActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://feeds.feedburner.com/MeneamePublicadas?format=xml";
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Register our self
		if ( getTabActivityTag().length() > 0 )
		{
			mApp.addTabActivity( getTabActivityTag(), this);
		}		
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String getTabActivityTag() {
		return "last_news_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int getIndicatorStringID() {
		return R.string.main_tab_news;
	}
}
