package com.guaranacode.android.fastpicasabrowser.util;

import android.app.ProgressDialog;

public class DialogUtil {
	/**
	 * If the dialog is still showing, completes the progress bar in this dialog and dismisses it.
	 * @param dialog
	 */
	public static void finishProgressDialog(ProgressDialog dialog) {
		if(dialog.isShowing()) {
			int diff = dialog.getMax() - dialog.getProgress();
			dialog.incrementProgressBy(diff);
			dialog.dismiss();
		}
	}
	
	/**
	 * Calculates and returns the remaining progress for this progress dialog.
	 * @param dialog
	 * @return
	 */
	public static int getRemainingProgress(ProgressDialog dialog) {
		if(null == dialog) {
			return 0;
		}
		
		return dialog.getMax() - dialog.getProgress();
	}
}
