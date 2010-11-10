package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class VersionChangesDialog extends Dialog {
	/** Log tag */
	private static final String TAG = "VersionChangesDialog";
	
	public VersionChangesDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		ApplicationMNM.addLogCat(TAG);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.version_change_dialog);
		setTitle(R.string.version_change_title);
		
		// When the user presses the ok button just dismiss the dialog
        Button btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
	           dismiss();
           }
        });
        
        LinearLayout mainContent = (LinearLayout)findViewById(R.id.version_change_content);
        for(int i = ApplicationMNM.getVersionNumber(); i > 0 ; i--)
        {
        	//////////////////////
        	// TITLE
        	//////////////////////
        	TextView title = new TextView(getContext());
        	
        	// Set layout params
        	MarginLayoutParams params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        	title.setLayoutParams(params);
			
			// More params
			title.setSingleLine(true);
			title.setAutoLinkMask(Linkify.WEB_URLS);
			title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			
			// Set text
			int resID = getContext().getResources().getIdentifier("version_change_v"+i+"_title", "string", "com.dcg.meneame");
			title.setText(resID);
			
			// Add view
			mainContent.addView(title);
        	
			//////////////////////
        	// BODY
        	//////////////////////
			TextView body = new TextView(this.getContext());
			
			// Set layout params
        	params = new MarginLayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        	params.setMargins(10, 0, 0, 0);
        	body.setLayoutParams(params);
			
			// More params
        	body.setSingleLine(false);
        	body.setAutoLinkMask(Linkify.WEB_URLS);
        	
			// Set text
			resID = getContext().getResources().getIdentifier("version_change_v"+i, "string", "com.dcg.meneame");
			body.setText(resID);
			
			// Add view
			mainContent.addView(body);
        }      
        
        
        // TODO: Add automatically all versions!
        /*
        int resID = getContext().getResources().getIdentifier("version_change_v4_title", "string", "com.dcg.meneame");
        ApplicationMNM.logCat(TAG,getContext().getResources().getString(resID));
		/**/
	}

}
