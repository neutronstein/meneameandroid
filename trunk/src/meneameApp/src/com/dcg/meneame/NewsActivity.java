package com.dcg.meneame;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class NewsActivity extends FeedActivity {
	
	static final String[] COUNTRIES = new String[] {
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra"
			};
	
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
		
		setContentView(R.layout.meneo_list);
		
		setListAdapter(new ArrayAdapter<String>(this,
		          R.layout.meneo_listitem, R.id.title, COUNTRIES));
		
		ListView listView = getListView();
		
		// Set basic listview stuff
		listView.setTextFilterEnabled(true);
		
		// Add context menu
		listView.setOnCreateContextMenuListener( 
				new View.OnCreateContextMenuListener() {
					public void onCreateContextMenu(ContextMenu menu, View view,ContextMenu.ContextMenuInfo menuInfo) {
						menu.add(0, 0, 0, R.string.meneo_item_open);
						menu.add(0, 0, 0, R.string.meneo_item_vote);
					}
	
				});
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
