package com.dcg.meneame;

import com.dcg.adapter.FeedItemAdapter;
import com.dcg.app.ApplicationMNM;
import com.dcg.provider.FeedItemElement;
import com.dcg.provider.SystemValue;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ListView;

public class DetailedArticleActivity extends ListActivity {
	
	/** Log tags */
	private static final String TAG = "DetailedArticleActivity";
	
	/** Are we paused or not? */
    protected boolean mbIsPaused;
    
    /** Our cached main list view */
	private ListView mListView = null;
    
    public DetailedArticleActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);
	}
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, "onCreate()");
	
		// Unpause
		mbIsPaused = false;
		
		// Perpare layout
		setContentView(R.layout.detailed_article);
		
		// Do final stuff
		setupViews();
	}
	
	/**
	 * By default we will use articels
	 * @return
	 */
	public int getFeedItemType() {
		return FeedItemElement.TYPE_ARTICLE;
	}
	
	/**
	 * Set a cursor adapter for our list
	 */
	protected void setCursorAdapter() {
		// TODO: Use: setFilterText(queryString); to set the filter.
		mListView.setAdapter(new FeedItemAdapter(
				this, 
				FeedItemElement.FEEDID+"=?",
				new String[]{"-1"},
				getFeedItemType()));
	}
	
	/**
	 * Setup view
	 */
	protected void setupViews() {
		mListView = getListView();
		
		if ( mListView != null )
		{
			// Set adapter
			setCursorAdapter();
			
			// Set basic ListView stuff
			mListView.setTextFilterEnabled(false);
		}
		else
		{
			ApplicationMNM.warnCat(TAG,"No ListView found in layout for " + this.toString());
		}
	}
}
