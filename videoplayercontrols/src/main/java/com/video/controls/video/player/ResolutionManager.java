package com.video.controls.video.player;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;

import com.video.controls.R;
import com.video.controls.video.model.VideoResolution;

import java.util.ArrayList;

public class ResolutionManager {
    public static final String VIDEO_QUALITY_AUTO = "Auto";
    public static final String SHARED_PREFERENCE_FILE = "com.video.controls.Video_Shared_preference";

    public static void showResolutionPopup(final Context context, ArrayList<VideoResolution> resolutions, final DialogInterface.OnClickListener onClickListener, final DialogInterface.OnDismissListener onDismissListener) {
        String[] arr = new String[resolutions.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = resolutions.get(i).getRes();
        }

        Dialog dialog = new AlertDialog.Builder(context/*, R.style.Theme_AppCompat_Dialog*/).setTitle(context.getResources().getString(R.string.video_resolution_dialog_title)).setSingleChoiceItems(arr, getCurrentQualityPos((Activity) context, resolutions), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {
                onClickListener.onClick(dialog, item);
                dialog.dismiss();
            }
        }).setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onDismissListener.onDismiss(dialog);
            }
        });
        dialog.show();
    }


    public static int getCurrentQualityPos(Activity context, ArrayList<VideoResolution> resolutions) {
        int currentResPos = 0;
        String curentResolutionString = getResolution(context);
        int curentResolutionInt = 0;
        try {
            curentResolutionInt = VIDEO_QUALITY_AUTO.equalsIgnoreCase(curentResolutionString) ? 0 : Integer.parseInt(curentResolutionString.substring(0, curentResolutionString.length() - 1));
        } catch (Exception invalidNumberException) {
            invalidNumberException.printStackTrace();
        }

        for (int i = 0; i < resolutions.size(); i++) {
            String resString = resolutions.get(i).getRes();
            int resInt = 0;
            try {
                resInt = VIDEO_QUALITY_AUTO.equalsIgnoreCase(resString) ? 0 : Integer.parseInt(resString.substring(0, resString.length() - 1));
            } catch (Exception invalidNumberException) {
                invalidNumberException.printStackTrace();

            }
            if (resInt <= curentResolutionInt) {
                currentResPos = i;
            }
        }
        return currentResPos;
    }


    public static String getResolution(Activity context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        return sharedPref.getString("resolution", VIDEO_QUALITY_AUTO);
    }

    public static void saveResolution(Activity context, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("resolution", value);
        editor.commit();
    }

}
