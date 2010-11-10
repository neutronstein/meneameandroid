package com.dcg.meneame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import com.dcg.app.ApplicationMNM;
import com.dcg.dialog.VersionChangesDialog;
import com.dcg.provider.FeedItemElement;

/**
 * Our preference activity
 * @author Moritz Wundke (b.thax.dcg@gmail.com)
 */
public class Preferences extends PreferenceActivity {
	/** Class tag used for it's logs */
	private static final String TAG = "Preferences";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.addLogCat(TAG);        
        ApplicationMNM.logCat(TAG, "onCreate()");
		
		// Add prefs from xml
		addPreferencesFromResource(R.xml.preferences);
		
		// Set version title!
		PreferenceScreen prefScreen = getPreferenceScreen();
		if ( prefScreen != null )
		{
			PreferenceGroup appPrefernce = (PreferenceGroup)prefScreen.getPreference(1);
			if ( appPrefernce != null )
			{
				Preference versionPrefernce = appPrefernce.getPreference(0);
				if ( versionPrefernce != null )
				{
					String versionTitle = getResources().getString(getResources().getIdentifier("version_title", "string", "com.dcg.meneame"));
					versionTitle = versionTitle.replaceAll("NUMBER", String.valueOf(ApplicationMNM.getVersionNumber()));
					versionTitle = versionTitle.replaceAll("LABLE", ApplicationMNM.getVersionLable());
					versionPrefernce.setTitle(versionTitle);
				}
			}
		}
	}
	
	/**
	 * Return storage type used
	 * @return
	 */
	public String getStorageType() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());        
        return prefs.getString("pref_app_storage", "SDCard");
	}
	
	public void  onContentChanged()
	{
		ApplicationMNM.logCat(TAG, "onContentChanged()");
		super.onContentChanged();
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		ApplicationMNM.logCat(TAG, "onContentChanged()");
		if ( preference.getKey().compareTo("pref_app_version_number") == 0 )
		{
			VersionChangesDialog versionDialog = new VersionChangesDialog(this);
        	versionDialog.show();
		}
		else if ( preference.getKey().compareTo("pref_app_clearcache") == 0 )
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.confirm_clear_cache)
				.setCancelable(false)
				.setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						clearFeedCache();
						dialog.dismiss();
					}
				})
				.setNegativeButton(R.string.generic_no, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
			AlertDialog clearCacheDialog = builder.create();
			clearCacheDialog.show();
		}
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
	/**
	 * Worker method that clears the current feed cache
	 * @return
	 */
	public boolean clearFeedCacheWorker() {
		try {
			getContentResolver().delete(FeedItemElement.CONTENT_URI, "", null);		
			return true;
		} catch( SQLException e ) {
			return false;
		}
	}
	
	/**
	 * Clear feed cache, file or DB.
	 */
	public void clearFeedCache() {		
		if ( clearFeedCacheWorker() )
		{
			ApplicationMNM.logCat(TAG, "Cache has been cleared!");
			ApplicationMNM.showToast(R.string.clear_cache_successfull);
		}
		else
		{
			ApplicationMNM.logCat(TAG, "Nothing to be deleted!");
			ApplicationMNM.showToast(R.string.clear_cache_failed);
		}
	}

}
