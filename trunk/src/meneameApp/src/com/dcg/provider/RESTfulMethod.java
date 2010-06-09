package com.dcg.provider;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class RESTfulMethod implements BaseColumns {
	
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
	
	/** Some database stuff */
	 private static String[] sArguments1 = new String[1];
	 private static String sSelection;
	
	/** Internal data */
	private String mName;
	private String mRequest;
	private int mStatus;
	private int mMethod;
	
	static {
        StringBuilder selection = new StringBuilder();
        selection.append(REQUEST);
        selection.append("=?");
        sSelection = selection.toString();
	}
	
	/** Build the right content values */
	public ContentValues getContentValues() {
		final ContentValues values = new ContentValues();

		values.put(NAME, mName);
		values.put(REQUEST, mRequest);
		values.put(STATUS, mStatus);
		values.put(METHOD, mMethod);
		
		return values;
	}
	
	/** Get the selection string used by this object */
	public String getSelection() {
		return sSelection;
	}
	
	/** Return the selection arguments */
	public String[] getSelectionArgs() {
		final String[] arguments1 = sArguments1;
		arguments1[0] = mRequest;
		return arguments1;
	}

	public String getmName() {
		return mName;
	}

	public void setmName(String mName) {
		this.mName = mName;
	}

	public String getmRequest() {
		return mRequest;
	}

	public void setmRequest(String mRequest) {
		this.mRequest = mRequest;
	}

	public int getmStatus() {
		return mStatus;
	}

	public void setmStatus(int mStatus) {
		this.mStatus = mStatus;
	}

	public int getmMethod() {
		return mMethod;
	}

	public void setmMethod(int mMethod) {
		this.mMethod = mMethod;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String NEW_LINE = System.getProperty("line.separator");

		// Start Object
		result.append(this.getClass().getName() + " Object {" + NEW_LINE);

		// Add data
		result.append(" mName: " + mName + NEW_LINE);
		result.append(" mRequest: " + mRequest + NEW_LINE);
		result.append(" mStatus: " + mStatus + NEW_LINE);
		result.append(" mMethod: " + mMethod + NEW_LINE);

		// End object
		result.append("}");
		return result.toString();
	}
}
