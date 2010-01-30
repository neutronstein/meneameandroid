package com.dcg.meneame;

import java.util.concurrent.Semaphore;

import android.app.Activity;

public class RssWorkerThread extends Thread {
	
	/** Make this theard thread safe */
	//public static final Semaphore mSemaphore = new Semaphore(1);
	
	/**
	 * 
	 * @param Activity ParentActivity, holds the semaphore to make this thread save
	 */
	public RssWorkerThread() {
		super();
	}
	
	@Override
	public void run() {
//		try {
//			try {
//				this.Semaphore.acquire();
//			} catch (InterruptedException e) {
//					return;
//			}
//			guardedRun();
//		} catch (InterruptedException e) {
//			// fall thru and exit normally
//		} finally {
//			Semaphore.release();
//		}
	}
	
	private void guardedRun() throws InterruptedException {
		// At this point we are thread safe
	}
}
