/**
 * Copyright 2014 Google Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.video.controls.video.videoad;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * Contains utility functions which are used by a number of other classes.
 */
public class Util {

    /**
     * Create a LayoutParams object for the given view which enforces a given width and height.
     *
     * <p>This method is a bit complicated because the TYPE of the LayoutParams that a view must
     * receive (ex. LinearLayout.LayoutParams, RelativeLayout.LayoutParams) depends on the type of its
     * PARENT view.
     *
     * <p>Thus, in this method, we look at the parent view of the given view, determine its type,
     * and create the appropriate LayoutParams for that type.
     *
     * <p>This method only supports views which are nested inside a FrameLayout, LinearLayout, or
     * GridLayout.
     */
    public static ViewGroup.LayoutParams getLayoutParamsBasedOnParent(View view, int width, int height)
            throws IllegalArgumentException {

        // Get the parent of the given view.
        ViewParent parent = view.getParent();

        // Determine what is the parent's type and return the appropriate type of LayoutParams.
        if (parent instanceof FrameLayout) {
            return new FrameLayout.LayoutParams(width, height);
        }
        if (parent instanceof RelativeLayout) {
            return new RelativeLayout.LayoutParams(width, height);
        }
        if (parent instanceof LinearLayout) {
            return new LinearLayout.LayoutParams(width, height);
        }

        // Throw this exception if the parent is not the correct type.
        IllegalArgumentException exception = new IllegalArgumentException("The PARENT of a " +
                "FrameLayout container used by the GoogleMediaFramework must be a LinearLayout, " +
                "FrameLayout, or RelativeLayout. Please ensure that the container is inside one of these " +
                "three supported view groups.");

        // If the parent is not one of the supported types, throw our exception.
        throw exception;
    }


    /**
     * Do not play video ad if content video length is less then 30 sec
     *
     * @return
     */
    public static boolean videoLengthCheck(String videoLength) {
        boolean isLenghtGreater = true;
        if (!TextUtils.isEmpty(videoLength)) {
            String[] arrayLenght = videoLength.split(":");
            int minute = 0;
            int second;
            if (arrayLenght.length > 1) {
                minute = Integer.parseInt(arrayLenght[0]);
                second = Integer.parseInt(arrayLenght[1]);
            } else {
                second = Integer.parseInt(arrayLenght[0]);
            }
            if (minute == 0 && second < 30) {
                isLenghtGreater = false;
            }
        }
        return isLenghtGreater;
    }


    public static boolean hasInternetAccess(Context context) {
        ConnectivityManager mCM = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mCM.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


    public static boolean isFullscreen(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }


    public static void changeStatusBarStatus(Context context, boolean isvisible) {

        if (isvisible) {

            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) actionBar.show();

            if (Build.VERSION.SDK_INT < 16) {
                ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = ((Activity) context).getWindow().getDecorView();
                // Show Status Bar.
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                decorView.setSystemUiVisibility(uiOptions);
            }
        } else {

            ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
            if (actionBar != null) actionBar.hide();
            // Hide Status Bar
            if (Build.VERSION.SDK_INT < 16) {
                ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            } else {
                View decorView = ((Activity) context).getWindow().getDecorView();
                // Hide Status Bar.
                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                decorView.setSystemUiVisibility(uiOptions);
            }
        }
    }


    public static int convertDPToPixels(int dps, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, context.getResources().getDisplayMetrics());
    }


    /**
     * Aspect ratio is 9:5
     */
    public static int get9x5Height(Context context) {
        int screenWidht = context.getResources().getDisplayMetrics().widthPixels;
        return (5 * screenWidht) / 9;
    }

}
