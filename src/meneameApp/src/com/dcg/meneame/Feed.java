package com.dcg.meneame;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class Feed extends Object {
	
	/** log tag for this class */
	private static final String TAG = "Feed";
	
	/** List of articles */
	private List<Article> mArticles=new ArrayList<Article>();
	
	/** Unique name of this feed, used by our caching process */
	private String nName;
	
	/** Publis date */
	public String mPubDate;
	
	public Feed() {
		super();
		
		// empty constructor
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "<FEED:nName:"+this.nName+";>";
	}
	
	/**
	 * Adds a new article int the feed
	 * @param article
	 */
	public void addArticle( Article article ) {
		if ( article != null )
		{
			Log.d(TAG, "Adding article: " + article);
			mArticles.add(0, article);
		}
	}
}
