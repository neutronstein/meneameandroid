package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

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
	
	public void clearFeedCache() {
		if ( ApplicationMNM.clearFeedCache() )
		{
			ApplicationMNM.logCat(TAG, "Cache has been cleared!");
			ApplicationMNM.showToast(R.string.clear_cache_successfull);
		}
		else
		{
			ApplicationMNM.logCat(TAG, "Failed to clear cache!");
			ApplicationMNM.showToast(R.string.clear_cache_failed);
		}
	}

}
