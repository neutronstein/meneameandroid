package com.dcg.meneame;


import android.os.Bundle;
public class CommentsActivity extends FeedActivity {
	
	static final int LIST_MENU_OPEN = 0;
	static final int LIST_MENU_VOTE = 1;
	
	public CommentsActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://www.meneame.net/comments_rss2.php";
		mbEnableOpenSourceContextOption = false;
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
		return "comments_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int getIndicatorStringID() {
		return R.string.main_tab_comments;
	}
}
