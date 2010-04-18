package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MeneameDbAdapter {
    private static final String TAG = "MeneameDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * TABLE: feed_cache
     */
    public static final String FEED_DATABASE_TABLE = "feed_cache";
    public static final String FEED_KEY_TAG = "tag";
	public static final String FEED_KEY_URL = "url";
	public static final String FEED_KEY_LAST_VISIBLE_POSITION = "lastVisible";
    public static final String FEED_KEY_ROWID = "_id";
    private static final String FEED_DATABASE_CREATE =
            "create table "+FEED_DATABASE_TABLE+" " +
    		"("+FEED_KEY_ROWID+" integer primary key autoincrement, " +
    		FEED_KEY_TAG+" text not null, " +
    		FEED_KEY_LAST_VISIBLE_POSITION+" integer not null," +
    		FEED_KEY_URL+" text not null);";
    
    /**
     * TABLE: items_cache
     */
    public static final String ITEMS_DATABASE_TABLE = "items_cache";
    private static final String ITEMS_DATABASE_CREATE = "";
    
    /**
     * TABLE: system
     */
    public static final String SYSTEM_DATABASE_TABLE = "system";
    public static final String SYSTEM_KEY_KEY = "key";
    public static final String SYSTEM_KEY_VALUE = "value";
    private static final String SYSTEM_DATABASE_CREATE =
    	"create table "+SYSTEM_DATABASE_TABLE+" " +
    	"("+SYSTEM_KEY_KEY+" text primary key, " +
    	SYSTEM_KEY_VALUE+" text);";
    
    /** List of tables */
    public static final String[] DATABASE_TABLES = { 
    	FEED_DATABASE_TABLE, 
    	ITEMS_DATABASE_TABLE, 
    	SYSTEM_DATABASE_CREATE };
    
    /** List of create table statements
     * NOTE: Needs to be in the same order as the table list above! 
     */
    private static final String[] DATABASE_CREATE_STATEMENTS = { 
    	FEED_DATABASE_CREATE, 
    	ITEMS_DATABASE_CREATE, 
    	SYSTEM_DATABASE_CREATE };
    
    /** Internal DB name and version */
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	ApplicationMNM.logCat(TAG, "Creating tables");
        	createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        	ApplicationMNM.logCat(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
        	conditionalDropTables(db);
            onCreate(db);
        }
    }
    
    /**
     * Will drop any table that exists
     */
    private static void conditionalDropTables( SQLiteDatabase db ) {
    	int dbTablesNum = DATABASE_TABLES.length;
    	for ( int i = 0; i < dbTablesNum; i++ ) {
    		ApplicationMNM.logCat(TAG, "Dropping table: "+DATABASE_TABLES[i]);
    		try {
    			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLES[i]);
        	} catch ( Exception e ) {
        		ApplicationMNM.warnCat(TAG, "Unabe to upgrade Meneame database: "+e.toString());
        	}
    	}
    }
    
    /**
     * Will create all tables we need
     * @param db
     */
    private static void createTables( SQLiteDatabase db ) {
    	int dbTablesNum = DATABASE_CREATE_STATEMENTS.length;
    	for ( int i = 0; i < dbTablesNum; i++ ) {
    		ApplicationMNM.logCat(TAG, "Creating table: "+DATABASE_TABLES[i]);
    		try {
    			db.execSQL(DATABASE_CREATE_STATEMENTS[i]);
        	} catch ( Exception e ) {
        		ApplicationMNM.warnCat(TAG, "Unabe to upgrade Meneame database: "+e.toString());
        	}
    	}
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public MeneameDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public MeneameDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
    
    /**
     * Sets a specific system key
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValue( String key, String value ) {
    	ContentValues args = new ContentValues();
		args.put(SYSTEM_KEY_KEY, key);
		args.put(SYSTEM_KEY_VALUE, value);
		try {
		
	    	if (getSystemValue(key) != null )
	    	{
	    		// Update key!
	            return mDb.update(SYSTEM_DATABASE_TABLE, args, SYSTEM_KEY_KEY + "=" + key, null) > 0;
	    	}
	    	else
	    	{
	    		// Insert key
	            return mDb.insert(SYSTEM_DATABASE_TABLE, null, args) > 0;
	    	}
		} catch( Exception e ) {
			return false;
		}
    }
    
    /**
     * Set a boolean system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueBool( String key, boolean value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Set an integer system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueInt( String key, int value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Set a float system value
     * @param key
     * @param value
     * @return
     */
    public boolean setSystemValueFloat( String key, float value ) {
    	return setSystemValue(key, String.valueOf(value));
    }
    
    /**
     * Get a system value associated with a specific key. Returns 
     * null if the keys dose not exists.
     * @param key
     * @return
     */    
    public String getSystemValue( String key ) {
    	Cursor mCursor = mDb.query(true, SYSTEM_DATABASE_TABLE,
    			new String[] {SYSTEM_KEY_VALUE}, 
            	SYSTEM_KEY_KEY + "=" + key,
            	null, null, null, null, null);
	    if (mCursor != null) 
	    {
	        mCursor.moveToFirst();
	        return mCursor.getString(0);
	    }
	    return null;
    }
    
    /**
     * Get a system value as a boolean value
     * @param key
     * @return
     */
    public boolean getSystemValueBool( String key, boolean defaultValue ) {
    	try {
	    	return Boolean.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		return defaultValue;
    	}
    }
    
    /**
     * Get a system value as an int value
     * @param key
     * @return
     */
    public int getSystemValueInt( String key, int defaultValue ) {
    	try {
	    	return Integer.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		return defaultValue;
    	}
    }
    
    /**
     * Get a system value as a float value
     * @param key
     * @return
     */
    public float getSystemValueFloat( String key, float defaultValue ) {
    	try {
	    	return Float.valueOf(getSystemValue( key ));
    	} catch( Exception e) {
    		return defaultValue;
    	}
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    /*
    public long createNote(String title, String body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }
    /**/

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    /*
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**/

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    /*
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, null, null, null, null, null);
    }
    /**/

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    /*
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_TITLE, KEY_BODY}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    /**/

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    /*
    public boolean updateNote(long rowId, String title, String body) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**/
}
