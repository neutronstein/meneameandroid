package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;
import com.dcg.util.rss.FeedItem;

/**
 * Class that represents an article contained by a feed
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class ArticleFeedItem extends FeedItem {
	/** Log tag */
	private static final String TAG = "ArticleFeedItem";
	
	public ArticleFeedItem()
	{
		super();
		ApplicationMNM.addLogCat(TAG);
		
		mPermittedList.add("title");
		mPermittedList.add("description");
		mPermittedList.add("votes");
		mPermittedList.add("url");
		mPermittedList.add("category");
		mPermittedList.add("link");
		mPermittedList.add("commentRss");
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
			try {
				// Get the real description
				int startIdx = rawValue.indexOf("<p>")+3;
				int endIdx = rawValue.indexOf("</p>");
				value = value.substring(startIdx, endIdx);
			} catch(Exception e) {
				// If this happens the sting is already cleaned up so just use the raw value
			}
			
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
		else if( key.equalsIgnoreCase("commentRss") )
		{
			try {
				String articleID = value.substring(44);
				//tmpValue = value.replaceAll("http://www.meneame.net/comments_rss2.php?id","joder");
				ApplicationMNM.logCat(TAG,"ArticleID: "+articleID);
				setArticleID(Integer.parseInt(articleID));
			} catch ( Exception e ) {
				// Nothing to be done here
				ApplicationMNM.warnCat(TAG,"ERROR: "+e.toString());
			}
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
	public boolean isKeyListValue( String key )
	{
		if ( key.trim().equals("category") )
		{
			return true;
		}
		return super.isKeyListValue(key);
	}
}
