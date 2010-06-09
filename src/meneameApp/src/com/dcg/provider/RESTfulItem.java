package com.dcg.provider;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class RESTfulItem implements BaseColumns {
	
	/** Define content provider connections */
	public static final String ELEMENT_AUTHORITY = "RESTfulmethod";
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.dcg.meneame/" + ELEMENT_AUTHORITY);

	/** DB table name */
	public static final String TABLE = "RESTful";
	
	/** Available method types */
	public static final int REST_GET = 0;
	public static final int REST_POST = 1;
	public static final int REST_INSERT = 2;
	public static final int REST_DELETE = 3;
	
	/** Available status types */
	public static final int STATUS_TRANSACTION = 0;
	public static final int STATUS_DONE = 1;
	public static final int STATUS_FAILED = 2;
	
	/** Table definition */
	public static final String NAME = "name";
	public static final int NAME_FIELD = 0;

	public static final String REQUEST = "request";
	public static final int REQUEST_FIELD = 1;
	
	public static final String STATUS = "status";
	public static final int STATUS_FIELD = 2;
	
	public static final String METHOD = "method";
	public static final int METHOD_FIELD = 3;
	
	/** Internal data */
	private String mName;
	
	/** Build the right content values */
	public static ContentValues getContentValue(String name, String request, int status, int method) {
		final ContentValues values = new ContentValues();
		values.put(NAME, name);
		values.put(REQUEST, request);
		values.put(STATUS, status);
		values.put(METHOD, method);
		return values;
	}

}
