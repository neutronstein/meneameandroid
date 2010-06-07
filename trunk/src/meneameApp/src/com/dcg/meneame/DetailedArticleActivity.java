package com.dcg.meneame;

import com.dcg.adapter.FeedItemAdapter;
import com.dcg.app.ApplicationMNM;
import com.dcg.provider.FeedItemElement;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

public class DetailedArticleActivity extends FeedActivity {

	/** Log tags */
	private static final String TAG = "DetailedArticleActivity";

	/** Are we paused or not? */
	protected boolean mbIsPaused;

	/** Our cached main list view */
	private ListView mListView = null;
	
	/** Article ID used to get data and comments */
	private int mArticleID = 0;

	public DetailedArticleActivity() {
		super();
		ApplicationMNM.addLogCat(TAG);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.logCat(TAG, "onCreate()");
		
		// From the extra get the article ID so we can start getting the
		// comments
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mArticleID = Integer.parseInt(extras.getString(EXTRA_KEY_ARTICLE_ID));
			ApplicationMNM.showToast("Detailed view for: " + mArticleID);
		}
		else
		{
			ApplicationMNM.showToast("No article ID specified in extra bundle!");
		}

	}
	
	/**
	 * Set the content view we will use for the activity
	 */
	protected void setupContentView() {
		// Prepare layout
		setContentView(R.layout.detailed_article);
	}
}
