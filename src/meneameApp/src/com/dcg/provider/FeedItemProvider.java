package com.dcg.provider;

import java.util.HashMap;
import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class FeedItemProvider extends ContentProvider {
	private static final String TAG = "FeedItemProvider";
	
	private static final String DATABASE_NAME = "feeditems.db";
    private static final int DATABASE_VERSION = 1;
    
    /** Action id's */
    private static final int SEARCH = 1;
    private static final int ITEMS = 1;
    private static final int ITEM_ID = 2;
	
	private static final String AUTHORITY = "meneame";
    
    private static final UriMatcher URI_MATCHER;
    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        URI_MATCHER.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        URI_MATCHER.addURI(AUTHORITY, "item", ITEMS);
        URI_MATCHER.addURI(AUTHORITY, "item/#", ITEM_ID);
    }
    
    private static final HashMap<String, String> SUGGESTION_PROJECTION_MAP;
    static {
        SUGGESTION_PROJECTION_MAP = new HashMap<String, String>();
        SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
        		FeedItemElement.LINK_ID + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SUGGESTION_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
        		FeedItemElement._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        SUGGESTION_PROJECTION_MAP.put(FeedItemElement._ID, FeedItemElement._ID);
    }
    
    private SQLiteOpenHelper mOpenHelper;

    private Pattern[] mKeyPrefixes;
    private Pattern[] mKeySuffixes;

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
