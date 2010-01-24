package com.dcg.meneame;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class MeneameNewsActivity extends ListActivity {
	
	static final String[] COUNTRIES = new String[] {
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra",
			"Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra"
			};
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
						AdapterContextMenuInfo ContextMenu =(AdapterContextMenuInfo) menuInfo;
						menu.add(0, 0, 0, R.string.meneo_item_open);
						menu.add(0, 0, 0, R.string.meneo_item_vote);
					}
	
				});
	}
}
