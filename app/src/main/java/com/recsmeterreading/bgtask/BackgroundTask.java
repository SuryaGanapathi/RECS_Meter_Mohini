/*
 * BackgroundTask.java 
 */

package com.recsmeterreading.bgtask;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.recsmeterreading.Utils.Globals;
import com.recsmeterreading.Utils.Network;
import com.recsmeterreading.Utils.PrintLog;
import com.recsmeterreading.exceptions.BadRequestException;
import com.recsmeterreading.exceptions.InternalServerError;
import com.recsmeterreading.exceptions.JavaLangException;


/**
 * The Class BackgroundTask.
 * 
 * @author rjonnalagadda
 */
public final class BackgroundTask extends AsyncTask<Void, Void, Object> {
	private AlertDialog progressDialog;

	private static final String TAG = "BackgroundTask";

	/** The Background Thread Object. */
	private BackgroundThread bgtask;

	/** The context. */
	private Context context;

	/** is task cancelled. */
	boolean isCancelled;

	private String loadingMessage;

	/**
	 * Instantiates a new background task.
	 * 
	 * @param context
	 *            the context
	 * @param bgtask
	 *            the Background Thread Object
	 */
	public BackgroundTask(Context context, BackgroundThread bgtask) {
		this.context = context;
		this.bgtask = bgtask;
		this.loadingMessage = "";
	}

	public BackgroundTask(Context context, BackgroundThread bgtask, String loadingMessage) {
		this.context = context;
		this.bgtask = bgtask;
		this.loadingMessage = loadingMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		if (!loadingMessage.equals("")){
			ProgressDialogBox.show(context, new ProcessCancelListener() {
				@Override
				public void processCanceled() {
					isCancelled = true;
					cancel(true);
				}
			}, loadingMessage);
	}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Object doInBackground(Void... params) {
		PrintLog.print(TAG, "doInBackground...");
		if(Network.isAvailable(context)){
			return bgtask.runTask();
		}
		return new BadRequestException("No Internet Connection please connect an try again later");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	@Override
	protected void onPostExecute(Object result) {
		PrintLog.print(TAG, "onPostExecute...");
		if (!isCancelled) {
			ProgressDialogBox.close();
			if (result instanceof BadRequestException) {
				Globals.showToast(context, ((BadRequestException) result).getMessage());
			} else if (result instanceof InternalServerError) {
				Globals.showToast(context, ((InternalServerError) result).getMessage());
			}else if (result instanceof JavaLangException) {
				Globals.showToast(context, ((JavaLangException) result).getMessage());
			}  else {
				bgtask.taskCompleted(result);
			}
		}
	}

	@Override
	protected void onCancelled() {
		if (!isCancelled) {
			PrintLog.print(TAG, "onCancelled...");
			ProgressDialogBox.close();
			super.onCancelled();
		}
	}
}
