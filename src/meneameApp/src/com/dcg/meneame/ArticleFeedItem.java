package com.dcg.meneame;

import com.dcg.util.rss.FeedItem;

public class ArticleFeedItem extends FeedItem {
	
	public ArticleFeedItem()
	{
		super();
		
		mPermittedList.add("title");
		mPermittedList.add("description");
		mPermittedList.add("votes");
		mPermittedList.add("url");
		// TODO: Cats must to be added to a single line
		//mPermittedList.add("category");
		mPermittedList.add("link");
	}
	/**
	 * tranform the data from a raw value into a valid value
	 * @param key
	 * @param rawValue
	 * @return
	 */
	protected String tranformRAWValue( String key, String rawValue )
	{
		String value = super.tranformRAWValue(key,rawValue);
		if( key.equalsIgnoreCase("description") )
		{
			// Get the real description
			int startIdx = rawValue.indexOf("<p>")+3;
			int endIdx = rawValue.indexOf("</p>");
			value = value.substring(startIdx, endIdx);
			
			// Now strip any html tags
			value = value.replaceAll("\\<.*?>","");
			
			// Now it's time to convert HTML codes to real text :D
			// Taken from: http://www.ascii.cl/htmlcodes.htm
			// Not all are done so far!
			value = value.replaceAll("&#32;"," ");
			value = value.replaceAll("&#33;","!");
			value = value.replaceAll("&#34;","\"");
			value = value.replaceAll("&#35;","#");
			value = value.replaceAll("&#36;","$");
			value = value.replaceAll("&#37;","%");
			value = value.replaceAll("&#38;","&");
			value = value.replaceAll("&#39;","'");
			value = value.replaceAll("&#40;","(");
			value = value.replaceAll("&#41;",")");
			value = value.replaceAll("&#42;","*");
			value = value.replaceAll("&#43;","+");
			value = value.replaceAll("&#44;",",");
			value = value.replaceAll("&#45;","-");
			value = value.replaceAll("&#46;",".");
			value = value.replaceAll("&#47;","/");
			value = value.replaceAll("&#48;","0");
			value = value.replaceAll("&#49;","1");
			value = value.replaceAll("&#50;","2");
			value = value.replaceAll("&#51;","3");
			value = value.replaceAll("&#52;","4");
			value = value.replaceAll("&#53;","5");
			value = value.replaceAll("&#54;","6");
			value = value.replaceAll("&#55;","7");
			value = value.replaceAll("&#56;","8");
			value = value.replaceAll("&#57;","9");
			value = value.replaceAll("&#58;",":");
			value = value.replaceAll("&#59;",";");
			value = value.replaceAll("&#60;","<");
			value = value.replaceAll("&#61;","=");
			value = value.replaceAll("&#62;",">");
			value = value.replaceAll("&#63;","?");
		}
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
		String value = super.tranformRAWListValue(key,rawValue);
		return value;		
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
