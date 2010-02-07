package com.dcg.meneame;

import java.util.ArrayList;
import java.util.List;

public class Article extends Object {
	public String mUser;
	public String mVotes;
	public String mNegatives;
	public String mKarma;
	public String mComments;
	public String mCommentRSS;
	public String mPubDate;
	public String mTitle;
	public String mURL;
	public String mGUID;
	public String mThumbnail;
	public List<String> Categories=new ArrayList<String>();
	
	public Article() {
		super();
		
		// empty constructor
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "<ART:mUser:"+this.mUser+";mTitle:"+this.mTitle+">";
	}
}
