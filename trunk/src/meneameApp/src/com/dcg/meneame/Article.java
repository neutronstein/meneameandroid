package com.dcg.meneame;

import java.util.ArrayList;
import java.util.List;

public class Article extends Object {
	private String mTitle;
	private String mDescription;
	private String mMeneameLink;
	private String mLink;
	private String mCreator;
	private String mPubDate;
	private List<String> mTags=new ArrayList<String>();
	private String mCommentsFeed;
	private int mComments;
	private int mVotes;
	private int mKarma;
	
	/**
	 * @param mTitle
	 * @param mDescription
	 * @param mMeneameLink
	 * @param mLink
	 * @param mCreator
	 * @param mPubDate
	 * @param mTags
	 * @param mCommentsFeed
	 * @param mComments
	 * @param mVotes
	 * @param mKarma
	 */
	public Article(String mTitle, String mDescription, String mMeneameLink,
			String mLink, String mCreator, String mPubDate, List<String> mTags,
			String mCommentsFeed, int mComments, int mVotes, int mKarma) {
		super();
		this.mTitle = mTitle;
		this.mDescription = mDescription;
		this.mMeneameLink = mMeneameLink;
		this.mLink = mLink;
		this.mCreator = mCreator;
		this.mPubDate = mPubDate;
		this.mTags = mTags;
		this.mCommentsFeed = mCommentsFeed;
		this.mComments = mComments;
		this.mVotes = mVotes;
		this.mKarma = mKarma;
	}
	
	/**
	 * @return the mTitle
	 */
	public String getTitle() {
		return mTitle;
	}
	
	/**
	 * @param mTitle the mTitle to set
	 */
	public void setTitle(String mTitle) {
		this.mTitle = mTitle;
	}
	
	/**
	 * @return the mDescription
	 */
	public String getDescription() {
		return mDescription;
	}
	
	/**
	 * @param mDescription the mDescription to set
	 */
	public void setDescription(String mDescription) {
		this.mDescription = mDescription;
	}
	
	/**
	 * @return the mMeneameLink
	 */
	public String getMeneameLink() {
		return mMeneameLink;
	}
	
	/**
	 * @param mMeneameLink the mMeneameLink to set
	 */
	public void setMeneameLink(String mMeneameLink) {
		this.mMeneameLink = mMeneameLink;
	}
	
	/**
	 * @return the mLink
	 */
	public String getLink() {
		return mLink;
	}
	
	/**
	 * @param mLink the mLink to set
	 */
	public void setLink(String mLink) {
		this.mLink = mLink;
	}
	
	/**
	 * @return the mCreator
	 */
	public String getCreator() {
		return mCreator;
	}
	
	/**
	 * @param mCreator the mCreator to set
	 */
	public void setCreator(String mCreator) {
		this.mCreator = mCreator;
	}
	/**
	 * @return the mPubDate
	 */
	public String getPubDate() {
		return mPubDate;
	}
	
	/**
	 * @param mPubDate the mPubDate to set
	 */
	public void setPubDate(String mPubDate) {
		this.mPubDate = mPubDate;
	}
	
	/**
	 * @return the mTags
	 */
	public List<String> getTags() {
		return mTags;
	}
	
	/**
	 * @param mTags the mTags to set
	 */
	public void setTags(List<String> mTags) {
		this.mTags = mTags;
	}
	
	/**
	 * @return the mCommentsFeed
	 */
	public String getCommentsFeed() {
		return mCommentsFeed;
	}
	
	/**
	 * @param mCommentsFeed the mCommentsFeed to set
	 */
	public void setCommentsFeed(String mCommentsFeed) {
		this.mCommentsFeed = mCommentsFeed;
	}
	
	/**
	 * @return the mComments
	 */
	public int getComments() {
		return mComments;
	}
	
	/**
	 * @param mComments the mComments to set
	 */
	public void setComments(int mComments) {
		this.mComments = mComments;
	}
	
	/**
	 * @return the mVotes
	 */
	public int getVotes() {
		return mVotes;
	}
	
	/**
	 * @param mVotes the mVotes to set
	 */
	public void setVotes(int mVotes) {
		this.mVotes = mVotes;
	}
	
	/**
	 * @return the mKarma
	 */
	public int getKarma() {
		return mKarma;
	}
	
	/**
	 * @param mKarma the mKarma to set
	 */
	public void setKarma(int mKarma) {
		this.mKarma = mKarma;
	}
}
