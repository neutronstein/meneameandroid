package com.dcg.meneame;

import android.os.Bundle;

/**
 * Comments activity
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class CommentsActivity extends FeedActivity {
	
	static final int LIST_MENU_OPEN = 0;
	static final int LIST_MENU_VOTE = 1;
	
	public CommentsActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://www.meneame.net/comments_rss2.php";
		mbIsArticleFeed = false;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	/**
	 * Returns the class name of the list adapter we should use
	 * @return
	 */
	public String getListAdapterClassName() {
		return "com.dcg.meneame.CommentsAdapter";
	}

	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public String getTabActivityTag() {
		return "comments_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public int getIndicatorStringID() {
		return R.string.main_tab_comments;
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String static_getTabActivityTag() {
		return "comments_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int static_getIndicatorStringID() {
		return R.string.main_tab_comments;
	}
}
