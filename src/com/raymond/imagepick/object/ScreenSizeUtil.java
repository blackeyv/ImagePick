package com.raymond.imagepick.object;

import android.app.Activity;
import android.util.DisplayMetrics;

public class ScreenSizeUtil 
{	
	private static int screenW;
    private static int screenH;
	private static float screenDensity;

	public static void initScreen(Activity mActivity){
	    DisplayMetrics metric = new DisplayMetrics();
	    mActivity.getWindowManager().getDefaultDisplay().getMetrics(metric);
	    screenW = metric.widthPixels;
	    screenH = metric.heightPixels;
	    screenDensity = metric.density;
	}

    public static int getScreenW(){
        return screenW;
    }

    public static int getScreenH(){
        return screenH;
    }

    public static float getScreenDensity(){
        return screenDensity;
    }
}
