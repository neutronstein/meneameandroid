package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

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
		// TODO Auto-generated method stub
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

}
