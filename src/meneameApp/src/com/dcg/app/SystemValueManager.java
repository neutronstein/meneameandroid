package com.dcg.app;

import com.dcg.provider.SystemValue;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class SystemValueManager {
	private static final String TAG = "SystemValueManager";
	
	static {
		ApplicationMNM.addLogCat(TAG);
	}
	
	/**
	 * Set a specific persistent system value
	 * @param contentResolver
	 * @param key
	 * @param value
	 */
	public static void setSystemValue( ContentResolver contentResolver, String key, String value ) {
		try {
			final ContentValues values = new ContentValues();
			values.put(SystemValue.KEY, key);
			values.put(SystemValue.VALUE, value);			
			SystemValueItem systemValue = getSystemValue(contentResolver,key);
			
			// If the system value is already there just update it
			if ( systemValue == null ) {
				// Add new value
				contentResolver.insert(SystemValue.CONTENT_URI, values);
				ApplicationMNM.logCat(TAG, "[INSERT] SystemValue "+key+"("+value+") set");
			} else {
				// Use the URI to update the item
				if ( contentResolver.update(systemValue.mUri, values, null, null) > 0 )
				{
					ApplicationMNM.logCat(TAG, "[UPDATE] SystemValue "+key+"("+value+")");
				}
				else
				{
					ApplicationMNM.logCat(TAG, "[UPDATE] [FAILED] SystemValue "+key+"("+value+"): "+systemValue.mUri);
				}
			}			
		} catch (SQLException e) {
			ApplicationMNM.logCat(TAG, "Failed to set system value "+key+":" + e.toString());
		}
	}
	
	/**
	 * Recover a system value tag
	 * @param contentResolver
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static SystemValueItem getSystemValue( ContentResolver contentResolver, String key ) {
		String[] projection = new String[] {
				SystemValue._ID,
				SystemValue.VALUE
				};		
		final String[] selectionArgs = new String[1];
		selectionArgs[0] = key;
		final String selection = SystemValue.KEY + "=?";
		
		Cursor cur = contentResolver.query(SystemValue.CONTENT_URI, projection, selection, selectionArgs, null);
		if ( cur != null && cur.moveToFirst() )
		{
			SystemValueItem result = new SystemValueItem();
			result.mKey = key;
			result.mValue = cur.getString(cur.getColumnIndex(SystemValue.VALUE));
			
			// Create the item URI
			result.mUri = ContentUris.withAppendedId(SystemValue.CONTENT_URI, cur.getLong(cur.getColumnIndex(SystemValue._ID)));
			
			cur.close();
			ApplicationMNM.logCat(TAG, "[QUERY] SystemValue "+key+"("+result.mValue+") recovered");
			return result;
		}
		else
		{
			ApplicationMNM.logCat(TAG, "[QUERY] [FAILED] SystemValue "+key+" not found in DB");
			return null;
		}
	}
}
