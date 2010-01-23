package com.dcg.meneame;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MeneameMainActivity extends TabActivity  {
    private TabHost mTabHost;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mTabHost = getTabHost();
        
        // Add news tab
        TabSpec newsTab = mTabHost.newTabSpec("last_news_tab"); 
        newsTab.setContent(new Intent(this, MeneameNewsActivity.class)); 
        newsTab.setIndicator( getResources().getString(R.string.main_tab_news) ); 
        mTabHost.addTab(newsTab);
        
        // Add queue tab
        TabSpec queueTab = mTabHost.newTabSpec("in_que_tab"); 
        queueTab.setContent(new Intent(this, MeneameQueueActivity.class)); 
        queueTab.setIndicator( getResources().getString(R.string.main_tab_queue) ); 
        mTabHost.addTab(queueTab);
        
        // Add comments tab
        TabSpec commentsTab = mTabHost.newTabSpec("comments_tab"); 
        commentsTab.setContent(new Intent(this, MeneameCommentsActivity.class)); 
        commentsTab.setIndicator( getResources().getString(R.string.main_tab_comments) ); 
        mTabHost.addTab(commentsTab);
        
        // Set news tab as visible one
        mTabHost.setCurrentTab(0);
    }
}