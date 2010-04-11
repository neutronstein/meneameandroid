package com.dcg.meneame;

import com.dcg.app.ApplicationMNM;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class NotameActivity extends Activity {
	/** Class tag used for it's logs */
	private static final String TAG = "NotameActivity";
	
	/** Result codes used by this activity */
	private static int NOTAME_RESULT_CODE_SEND = 0;
	private static int NOTAME_RESULT_CODE_BACK = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationMNM.addLogCat(TAG);        
        ApplicationMNM.logCat(TAG, "onCreate()");
        
        setContentView(R.layout.notame);
        
        // Set button callbacks
        Button btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
        	   sendNotameMessage();
           }
        });
        
        Button btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
        	   back();
           }
        });
	}
	
	public void sendNotameMessage() {
		// Send
		NotameThread workerThread = new NotameThread();
		workerThread.start();
		
		// Finish
		setResult(NOTAME_RESULT_CODE_SEND, null);
        finish();
	}
	
	public void back() {
		ApplicationMNM.logCat(TAG, "Back");		
		setResult(NOTAME_RESULT_CODE_BACK, null);
        finish();
	}
	
	public class NotameThread extends Thread {

	    public void run() {
	    	boolean bResult = false;
	    	ApplicationMNM.logCat(TAG, "Sending nótame message");
	    	
	    	if ( bResult )
	    	{
	    		ApplicationMNM.logCat(TAG, " Finished!");
	    	}
	    	else
	    	{
	    		ApplicationMNM.warnCat(TAG, " Failed!");
	    	}
	    	
	    }
	}

}
