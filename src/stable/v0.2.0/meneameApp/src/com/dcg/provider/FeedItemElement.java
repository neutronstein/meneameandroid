package com.dcg.provider;

import java.util.ArrayList;
import java.util.List;

import com.dcg.util.TextUtilities;

import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public class FeedItemElement implements BaseColumns {
	
	/** Define content provider connections */
	public static final String ELEMENT_AUTHORITY = "items";
	public static final Uri CONTENT_URI = Uri.parse("content://com.dcg.meneame/"+ELEMENT_AUTHORITY);
	public static final String DEFAULT_SORT_ORDER = "_id ASC";
	
	/** Type definitions */
	public static final int TYPE_ARTICLE = 0;
	public static final int TYPE_COMMENT = 1;
	public static final int TYPE_MAX_COUNT = TYPE_COMMENT+1;
	
	/** DB table name */
	public static final String TABLE = "items";
	
	/** Table definition */
	public static final String LINK_ID = "link_id";
	public static final int LINK_ID_FIELD = 0;
	
    public static final String FEEDID = "feedId";
    public static final int FEEDID_FIELD = 1;
    
    public static final String COMMENT_RSS = "commentRss";
    public static final int COMMENT_RSS_FIELD = 2;
    
    public static final String TITLE = "title";
    public static final int TITLE_FIELD = 3;
    
    public static final String VOTES = "votes";
    public static final int VOTES_FIELD = 4;
    
    public static final String LINK = "link";
    public static final int LINK_FIELD = 5;
    
    public static final String DESCRIPTION = "description";
    public static final int DESCRIPTION_FIELD = 6;
    
    public static final String CATEGORY = "category";
    public static final int CATEGORY_FIELD = 7;
    
    public static final String URL = "url";
    public static final int URL_FIELD = 8;
    
    public static final String TYPE = "type";
    public static final int TYPE_FIELD = 9;
    
    /** Feed item data */
    private int mLinkID;
    private int mFeedID;
    private String mCommentRSS;
    private String mTitle;
    private int mVotes;
    private String mLink;
    private String mDescription;
    private List<String> mCategories; 
    private String mURL;
    private int mType;
    
    public FeedItemElement() {
    	mCategories = new ArrayList<String>();
    }
    
    public void setLinkID( int linkID ) {
    	mLinkID = linkID;
    }
    
    public int getLinkID() {
    	return mLinkID;
    }
    
    public void setFeedID( int feedID ) {
    	mFeedID = feedID;
    }
    
    public int getFeedID() {
    	return mFeedID;
    }
    
    public void setCommentRSS( String commentRSS ) {
    	mCommentRSS = commentRSS;
    }
    
    public String getCommentRSS()  {
    	return mCommentRSS;
    }
    
    public void setTitle( String title ) {
    	mTitle = title;
    }
    
    public String getTile() {
    	return mTitle;
    }
    
    public void setVotes( int votes ) {
    	mVotes = votes;
    }
    
    public int getVotes() {
    	return mVotes;
    }
    
    public void setLink( String link ) {
    	mLink = link;
    }
    
    public String getLink() {
    	return mLink;
    }
    
    public void setDescription( String description ) {
    	mDescription = description;
    	try {
			// Get the real description
			int startIdx = mDescription.indexOf("<p>")+3;
			int endIdx = mDescription.indexOf("</p>");
			mDescription = mDescription.substring(startIdx, endIdx);
		} catch(Exception e) {
			// If this happens the sting is already cleaned up so just use the raw value
		}
		
		// Now strip any html tags
		mDescription = mDescription.replaceAll("\\<.*?>","");
		
		// Now it's time to convert HTML codes to real text :D
		// Taken from: http://www.ascii.cl/htmlcodes.htm
		// Not all are done so far!
		mDescription = mDescription.replaceAll("&#32;"," ");
		mDescription = mDescription.replaceAll("&#33;","!");
		mDescription = mDescription.replaceAll("&#34;","\"");
		mDescription = mDescription.replaceAll("&#35;","#");
		mDescription = mDescription.replaceAll("&#36;","$");
		mDescription = mDescription.replaceAll("&#37;","%");
		mDescription = mDescription.replaceAll("&#38;","&");
		mDescription = mDescription.replaceAll("&#39;","'");
		mDescription = mDescription.replaceAll("&#40;","(");
		mDescription = mDescription.replaceAll("&#41;",")");
		mDescription = mDescription.replaceAll("&#42;","*");
		mDescription = mDescription.replaceAll("&#43;","+");
		mDescription = mDescription.replaceAll("&#44;",",");
		mDescription = mDescription.replaceAll("&#45;","-");
		mDescription = mDescription.replaceAll("&#46;",".");
		mDescription = mDescription.replaceAll("&#47;","/");
		mDescription = mDescription.replaceAll("&#48;","0");
		mDescription = mDescription.replaceAll("&#49;","1");
		mDescription = mDescription.replaceAll("&#50;","2");
		mDescription = mDescription.replaceAll("&#51;","3");
		mDescription = mDescription.replaceAll("&#52;","4");
		mDescription = mDescription.replaceAll("&#53;","5");
		mDescription = mDescription.replaceAll("&#54;","6");
		mDescription = mDescription.replaceAll("&#55;","7");
		mDescription = mDescription.replaceAll("&#56;","8");
		mDescription = mDescription.replaceAll("&#57;","9");
		mDescription = mDescription.replaceAll("&#58;",":");
		mDescription = mDescription.replaceAll("&#59;",";");
		mDescription = mDescription.replaceAll("&#60;","<");
		mDescription = mDescription.replaceAll("&#61;","=");
		mDescription = mDescription.replaceAll("&#62;",">");
		mDescription = mDescription.replaceAll("&#63;","?");
    }
    
    public String getDescription() {
    	return mDescription;
    }
    
    public void addCategory( String category ) {
    	if ( !mCategories.contains(category) )
    	{
    		mCategories.add(category);
    	}
    }
    
    public List<String> getCatogries() {
    	return mCategories;
    }
    
    public void setURL(String url ) {
    	mURL = url;
    }
    
    public String getURL() {
    	return mURL;
    }
    
    public void setType(int type) {
    	mType = type;
    }
    
    /**
     * Returns the type of the feed item.
     * @return TYPE_ARTICLE or TYPE_COMMENT
     */
    public int getType() {
    	return mType;
    }
    
    public void clear() {
    	mLinkID = -1;
        mFeedID = -1;
        mCommentRSS = "";
        mTitle = "";
        mVotes = 0;
        mLink = "";
        mDescription = "";
        mCategories.clear(); 
        mURL = "";
    }
    
    public ContentValues getContentValues() {
    	final ContentValues values = new ContentValues();
    	
    	values.put(LINK_ID, mLinkID);
    	values.put(FEEDID, mFeedID);
    	values.put(COMMENT_RSS, mCommentRSS);
    	values.put(TITLE, mTitle);
    	values.put(VOTES, mVotes);
    	values.put(LINK, mLink);
    	values.put(DESCRIPTION, mDescription);
    	values.put(CATEGORY, TextUtilities.join(mCategories, ", "));
    	values.put(URL, mURL);
    	values.put(TYPE, mType);
    	
    	return values;
    }
    
    @Override
    public String toString() {
    	StringBuilder result = new StringBuilder();
    	String NEW_LINE = System.getProperty("line.separator");
    	
    	// Start Object
    	result.append(this.getClass().getName() + " Object {" + NEW_LINE);
    	
    	// Add data
    	result.append(" mLinkID: " + mLinkID + NEW_LINE);
    	result.append(" mFeedID: " + mFeedID + NEW_LINE);
    	result.append(" mCommentRSS: " + mCommentRSS + NEW_LINE);
    	result.append(" mTitle: " + mTitle + NEW_LINE);
    	result.append(" mVotes: " + mVotes + NEW_LINE);
    	result.append(" mLink: " + mLink + NEW_LINE);
    	result.append(" mDescription: " + mDescription + NEW_LINE);
    	result.append(" mCategories: " + TextUtilities.join(mCategories, ", ") + NEW_LINE);
    	result.append(" mURL: " + mURL + NEW_LINE);
    	result.append(" mTYpe: " + mType + NEW_LINE);
    	
    	// End object
    	result.append("}");
    	return result.toString();
    }
}
