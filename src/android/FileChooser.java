package com.megster.cordova;

import java.util.Locale;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class FileChooser extends CordovaPlugin {

	private static final String TAG = "FileChooser";
	private static final String ACTION_OPEN = "open";
	private static final int PICK_FILE_REQUEST = 1;
	CallbackContext callback;

	@Override
	public boolean execute(String action, CordovaArgs args,
			CallbackContext callbackContext) throws JSONException {

		if (action.equals(ACTION_OPEN)) {
			chooseFile(callbackContext);
			return true;
		}

		return false;
	}

	public void chooseFile(CallbackContext callbackContext) {

		// type and title should be configurable

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

		Intent chooser = Intent.createChooser(intent, "Select File");
		cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

		PluginResult pluginResult = new PluginResult(
				PluginResult.Status.NO_RESULT);
		pluginResult.setKeepCallback(true);
		callback = callbackContext;
		callbackContext.sendPluginResult(pluginResult);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == PICK_FILE_REQUEST && callback != null) {

			if (resultCode == Activity.RESULT_OK) {

				Uri uri = data.getData();

				if (uri != null) {
					String result = null;
					if (uri.toString().toLowerCase(Locale.US)
							.startsWith("file://")) {
						result = uri.getPath();
						callback.success(result);
					} else if ((uri.toString().toLowerCase(Locale.US)
							.startsWith("content://"))) {
						result = getRealPathFromURI(cordova.getActivity(), uri);
						callback.success(result);
					} else {//fallback to original
						callback.success(uri.toString());
					}
					Log.w(TAG, uri.toString());
				} else {

					callback.error("File uri was null");

				}

			} else if (resultCode == Activity.RESULT_CANCELED) {

				// TODO NO_RESULT or error callback?
				PluginResult pluginResult = new PluginResult(
						PluginResult.Status.NO_RESULT);
				callback.sendPluginResult(pluginResult);

			} else {

				callback.error(resultCode);
			}
		}
	}

	public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}
}
