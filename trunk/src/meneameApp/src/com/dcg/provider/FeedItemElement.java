package com.dcg.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class FeedItemElement implements BaseColumns {
	public static final Uri CONTENT_URI = Uri.parse("content://meneame/content");
	public static final String DEFAULT_SORT_ORDER = "link_id DESC";
	
	/** Table definition */
	public static final String LINK_ID = "link_id";
    public static final String FEEDID = "feedId";    
    public static final String COMMENT_RSS = "commentRss";
    public static final String TITLE = "title";
    public static final String VOTES = "votes";
    public static final String LINK = "link";
    public static final String DESCRIPTION = "description";
    public static final String CATEGORY = "category";
    public static final String URL = "url";
}
