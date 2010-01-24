package com.dcg.meneame;

import java.util.prefs.Preferences;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MeneameMainActivity extends TabActivity  {
	
	/** Main app TabHost*/
	private TabHost mTabHost;
	
	/** Refresh menu item id */
	private static final int MENU_REFRESH = 0;
	
	/** Settings menu item id */
    private static final int MENU_SETTINGS = 1;
    
    /** Main animation */
    private Animation mMainAnimation = null;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mMainAnimation = AnimationUtils.loadAnimation( this, R.anim.slide_bottom );
        
        setContentView(R.layout.main);

        mTabHost = getTabHost();
        
        // Todo, make custom tabs: http://ezmobile.wordpress.com/2009/02/02/customized-android-tabs/
        
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
    
    /** After the activity get's visible to the user */
    protected void onResume() {
    	super.onResume();
    	mTabHost.startAnimation(mMainAnimation);    	
    }
    
    /* Creates the menu items */
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_REFRESH, 0, R.string.main_menu_refresh).setIcon(R.drawable.ic_menu_refresh);
        menu.add(0, MENU_SETTINGS, 0, R.string.main_menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_REFRESH:
            // Refresh currently selected tab content
            return true;
        case MENU_SETTINGS:
            // Open settitngs screen
        	openSettingsScreen();
            return true;
        }
        return false;
    }
    
    /* Open settings screen */
    public void openSettingsScreen() {
    	Intent settingsActivity = new Intent( this, MeneamePreferences.class);
    	startActivity(settingsActivity);
    }
}