package com.dcg.util.rss;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import com.dcg.app.ApplicationMNM;

/**
 * Base item our feed parser will return, must be subclassed to own types.
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class FeedItem implements Parcelable {
	
	/** log tag for this class */
	private static final String TAG = "FeedItem";
	
	/** Our map that holds the internal data */
	private Map<String, String> mItemData = null;
	
	/** our semaphore to make this class thread safe! */
	private Semaphore mSemaphore = new Semaphore(1);
	
	/** List of keys permitted */
	protected static List<String> mPermittedList = new ArrayList<String>();
	
	/** List of keys restricted */
	protected static List<String> mRestrictedList = new ArrayList<String>();

	/**
	 * Empty constructor
	 */
	public FeedItem() {	
		ApplicationMNM.addLogCat(TAG);		
		mItemData = new HashMap<String, String>();
	}
	
	public FeedItem(Parcel in) {		
		ApplicationMNM.addLogCat(TAG);		
		mItemData = new HashMap<String, String>();
		readFromParcel(in);
	}
	
	public static final Parcelable.Creator<FeedItem> CREATOR = new Parcelable.Creator<FeedItem>() {
        public FeedItem createFromParcel(Parcel in) {
            return new FeedItem(in);
        }
 
        public FeedItem[] newArray(int size) {
            return new FeedItem[size];
        }
    };
    
    public int describeContents() {
		return 0;
	}
    
    /**
     * Write all our data to a parcel
     */
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(mItemData.size());
    	for (String s: mItemData.keySet()) {
    		dest.writeString(s);
    		dest.writeString(mItemData.get(s));
    	}
	}
	
    /**
     * Fill up all data from a parcel
     * @param in
     */
    public void readFromParcel(Parcel in) {
    	int count = in.readInt();
    	for ( int i = 0; i < count; i++ )
    		setValue(in.readString(), in.readString());
    }
    
	/**
	 * Will acquire internal semaphore
	 * @throws InterruptedException
	 */
	private void acquireSemaphore() throws InterruptedException
	{
		mSemaphore.acquire();
	}
	
	/**
	 * release internal semaphore
	 */
	private void releaseSemaphore()
	{
		mSemaphore.release();
	}
	
	/**
	 * Looks if this is a valid key or not
	 * @param key
	 * @return
	 */
	public boolean isKeyValid( String key )
	{
		return isKeyPermitted(key) && !isKeyRestricted(key);
	}
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key )
	{
		return mPermittedList.contains(key);
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key )
	{
		return mRestrictedList.contains(key);
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key )
	{
		return false;
	}
	
	/**
	 * Will set the object value for a key.
	 * @param key
	 * @param value
	 * @return true if added and valid or false if not valid or adding failed
	 */
	public boolean setValue( String key, Object value )
	{
		if ( !isKeyValid(key) ) return false;
		try {
			return (isKeyListValue(key))?setListItemValue(key, (String) value):setStringValue(key, (String) value);
		} catch ( Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWValue( String key, String rawValue )
	{
		String value = rawValue;
		return value;		
	}
	
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWListValue( String key, String rawValue )
	{
		String value = rawValue;
		return value;		
	}
	
	/**
	 * Set a string value
	 * @param key
	 * @param value
	 * @return true/false
	 */
	protected boolean setStringValue( String key, String value )
	{
		boolean bResult;
		String finalValue = value;
		bResult = false;		
		try {
			// Make us tread safe!
			acquireSemaphore();
			ApplicationMNM.logCat(TAG,"setStringValue::("+ key +") value("+ value +")");
			finalValue = tranformRAWValue(key,value);
			setKeyValue(key, finalValue);
			bResult = true;
		} catch( Exception e) {
			// fall thru and exit normally
			ApplicationMNM.warnCat(TAG,"(setStringValue) Can not set key("+ key +") value("+ finalValue +")");
		} finally {
			// release our semaphore
			releaseSemaphore();
		}
		return bResult;
	}

	/**
	 * Add a value as an item of a list
	 * @param key
	 * @param value
	 * @return true/false
	 */
	protected boolean setListItemValue( String key, String value )
	{
		// TODO: Add list functionality
		return false;
//		boolean bResult;
//		String finalValue = value;
//		bResult = false;		
//		try {
//			// Make us tread safe!
//			acquireSemaphore();
//			
//			finalValue = tranformRAWValue(key,value);
//			
//			// Create or add a new item
//			List<String> itemList = null;
//			if ( !containsKey(key) )
//			{			
//				// No item there so build the list item
//				itemList = new ArrayList<String>();
//				
//				// Add the new value
//				itemList.add(finalValue);
//			}
//			else
//			{
//				Object rawValue = getKeyData(key);
//				itemList = (rawValue != null)?(List<String>) rawValue:null;
//				if ( itemList != null )
//				{
//					itemList.add(finalValue);
//				}
//			}
//			
//			// Update item
//			if ( itemList != null )
//			{
//				ApplicationMNM.logCat(TAG,"setListItemValue::("+ key +") value("+ finalValue +")");
//				setKeyValue(key,itemList);
//				bResult = true;
//			}
//		} catch( Exception e) {
//			// fall thru and exit normally
//			ApplicationMNM.warnCat(TAG,"(setListItemValue) Can not set key("+ key +") value("+ finalValue +")");
//		} finally {
//			// release our semaphore
//			releaseSemaphore();
//		}
//		return bResult;
	}
	
	/**
	 * Add a new data set
	 * @param key
	 * @param value
	 */
	private void setKeyValue( String key, String value ) {
		mItemData.put(key, value);
	}
	
	public void removeKey( String key )
	{
		mItemData.remove(key);
	}
	
	/**
	 * 
	 * @param key
	 * @return true/false if the key is included
	 */
	public boolean containsKey( String key )
	{
		return mItemData.containsKey(key);
	}
	
	/**
	 * Return the data value object for a specifc key
	 * @param key
	 * @return value
	 */
	public Object getKeyData( String key )
	{
		return mItemData.get(key);
	}
	
	/**
	 * Return the map of data
	 * @return Map of Key/Value
	 */
	public Map<String, String> getAllData()
	{		
		return mItemData;
	}
}
