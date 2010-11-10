package com.dcg.meneame;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VersionChangesDialog extends Dialog {

	public VersionChangesDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
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
	}

}
