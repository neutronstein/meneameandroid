package com.dcg.meneame;

import android.os.Bundle;

/**
 * News activity
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class NewsActivity extends FeedActivity {
	
	public NewsActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://feeds.feedburner.com/MeneamePublicadas?format=xml?local";
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public String getTabActivityTag() {
		return "last_news_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public int getIndicatorStringID() {
		return R.string.main_tab_news;
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "last_news_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return R.string.main_tab_news;
	}
}
