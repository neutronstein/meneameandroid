package com.dcg.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class FeedItemElement implements BaseColumns {
	public static final Uri CONTENT_URI = Uri.parse("content://com.dcg.meneame/items");
	public static final String DEFAULT_SORT_ORDER = "link_id DESC";
	
	/** DB table name */
	public static final String TABLE = "items";
	
	/** Table definition */
	public static final String LINK_ID = "link_id";
	public static final int LINK_ID_FIELD = 0;
	
    public static final String FEEDID = "feedId";
    public static final int FEEDID_FIELD = 1;
    
    public static final String COMMENT_RSS = "commentRss";
    public static final int COMMENT_RSS_FIELD = 2;
    
    public static final String TITLE = "title";
    public static final int TITLE_FIELD = 3;
    
    public static final String VOTES = "votes";
    public static final int VOTES_FIELD = 4;
    
    public static final String LINK = "link";
    public static final int LINK_FIELD = 5;
    
    public static final String DESCRIPTION = "description";
    public static final int DESCRIPTION_FIELD = 6;
    
    public static final String CATEGORY = "category";
    public static final int CATEGORY_FIELD = 7;
    
    public static final String URL = "url";
    public static final int URL_FIELD = 8;
}
