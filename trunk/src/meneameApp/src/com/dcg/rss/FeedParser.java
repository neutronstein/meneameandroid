package com.dcg.rss;

import com.dcg.app.ApplicationMNM;

/**
 * Our feed parser
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class FeedParser extends com.dcg.rss.RSSParser {
	
	/** log tag for this class */
	private static final String TAG = "FeedParser";

	/**
	 * Create a RSSParser passing along a RSS RawData
	 * @param RawFeed
	 */
	public FeedParser()
    {
        // Set our feed item
        this.setmFeedItemClassName("com.dcg.rss.ArticleFeedItem");
        
        // Add our tag to the category log (so it will be printed out)
        ApplicationMNM.addLogCat(TAG);
    }
}
