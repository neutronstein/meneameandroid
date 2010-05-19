package com.dcg.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class SystemValue implements BaseColumns {
	public static final Uri CONTENT_URI = Uri.parse("content://com.dcg.meneame/systemvalue");
	
	/** DB table name */
	public static final String TABLE = "system";
	
	/** Table definition */
	public static final String KEY = "key";
	public static final int KEY_FIELD = 0;
	
    public static final String VALUE = "value";
    public static final int VALUE_FIELD = 1;
}