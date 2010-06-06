package com.dcg.provider;

import com.dcg.util.TextUtilities;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class SystemValue implements BaseColumns {
	
	/** Define content provider connections */
	public static final String ELEMENT_AUTHORITY = "systemvalues";
	public static final Uri CONTENT_URI = Uri.parse("content://com.dcg.meneame/"+ELEMENT_AUTHORITY);
	
	/** DB table name */
	public static final String TABLE = "system";
	
	/** Table definition */
	public static final String KEY = "key";
	public static final int KEY_FIELD = 0;
	
    public static final String VALUE = "value";
    public static final int VALUE_FIELD = 1;
    
    /** Build a content value struct */
    public static ContentValues getContentValue( String key, String value) {
    	final ContentValues values = new ContentValues();    	
    	values.put(key, value);    	
    	return values;
    }
}