package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        
        // TODO: Add automatically all versions!
        /*
        int resID = getContext().getResources().getIdentifier("version_change_v4_title", "string", "com.dcg.meneame");
        ApplicationMNM.logCat(TAG,getContext().getResources().getString(resID));
		/**/
	}

}
