package com.dcg.meneame;

public class ArticleFeedItem extends FeedItem {
	
	/**
	 * Decides if a key can be added to the feed item, by default returns true
	 * @param key
	 * @return
	 */
	protected boolean isKeyPermitted( String key )
	{
		return super.isKeyPermitted(key);
	}
	
	/**
	 * Is this a restricted key?
	 * @param key
	 * @return
	 */
	protected boolean isKeyRestricted( String key )
	{
		boolean bResult = false;
		if ( key.trim().equals("thumbnail") )
		{
			bResult = true;
		}
		return bResult || super.isKeyRestricted(key);
	}
	
	/**
	 * Looks if the key should be a list or not
	 * @param key
	 * @return
	 */
	protected boolean isKeyListValue( String key )
	{
		if ( key.trim().equals("category") )
		{
			return true;
		}
		return super.isKeyListValue(key);
	}
}
