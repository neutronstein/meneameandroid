package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

public class FeedParser extends com.dcg.util.rss.RSSParser {
	
	/** log tag for this class */
	private static final String TAG = "FeedParser";

	/**
	 * Create a RSSParser passing along a RSS RawData
	 * @param RawFeed
	 */
	public FeedParser()
    {
        // Set our feed item
        this.setmFeedItemClassName("com.dcg.meneame.ArticleFeedItem");
        
        // Add our tag to the category log (so it will be printed out)
        ApplicationMNM.AddLogCat(TAG);
    }
}
