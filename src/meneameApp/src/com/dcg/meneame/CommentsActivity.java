package com.dcg.meneame;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CommentsActivity extends FeedActivity {
	
	static final String[] COUNTRIES = new String[] {
		"Belize", "Benin", "Bermuda", "Bhutan", "Bolivia",
	    "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory",
	    "British Virgin Islands", "Brunei", "Bulgaria", "Burkina Faso", "Burundi",
	    "Cote d'Ivoire", "Cambodia", "Cameroon", "Canada", "Cape Verde",
		};
	
	static final int LIST_MENU_OPEN = 0;
	static final int LIST_MENU_VOTE = 1;
	
	public CommentsActivity() {
		super();
		
		// Set feed
		mFeedURL = "http://www.meneame.net/comments_rss2.php";
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
						//AdapterContextMenuInfo ContextMenu =(AdapterContextMenuInfo) menuInfo;
						menu.add(0, LIST_MENU_OPEN, 0, "Open");
						menu.add(0, LIST_MENU_VOTE, 0, "Vote");
					}
	
				});
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
