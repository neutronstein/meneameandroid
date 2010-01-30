package com.dcg.meneame;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class QueueActivity extends FeedActivity {
	
	static final String[] COUNTRIES = new String[] {
		"Estonia", "Ethiopia", "Faeroe Islands", "Falkland Islands", "Fiji", "Finland",
	    "Former Yugoslav Republic of Macedonia", "France", "French Guiana", "French Polynesia",
	    "French Southern Territories", "Gabon", "Georgia", "Germany", "Ghana", "Gibraltar",
	    "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guinea", "Guinea-Bissau",
	    "Guyana", "Haiti", "Heard Island and McDonald Islands", "Honduras", "Hong Kong", "Hungary",
		};
	
	public QueueActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://feeds.feedburner.com/MeneameEnCola?format=xml";
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
						menu.add(0, 0, 0, "Open");
						menu.add(0, 0, 0, "Vote");
					}
	
				});
	}
	
	/**
	 * Returns the tag this activity will hold in the main TabWidget
	 * @return String - TabTag
	 */
	public static String getTabActivityTag() {
		return "in_que_tab";
	}
	
	/**
	 * String id used for the tab indicator
	 * @return
	 */
	public static int getIndicatorStringID() {
		return R.string.main_tab_queue;
	}
}
