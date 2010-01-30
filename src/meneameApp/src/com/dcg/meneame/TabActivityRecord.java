package com.dcg.meneame;

import android.app.Activity;

/**
 * Holds an activity and a tag used to recover the activity on
 * Tab related actions
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 *
 */

public class TabActivityRecord extends Object {
	private String mTabTag = "";
	private Activity mTabActivity = null;
	
	public TabActivityRecord( String tag, Activity activity ) {
		super();
		
		// Fill-up
		mTabTag = tag;
		mTabActivity = activity;
	}
	
	public String getTabTag() {
		return mTabTag;
	}
	
	public Activity getTabActivity() {
		return mTabActivity;
	}
}