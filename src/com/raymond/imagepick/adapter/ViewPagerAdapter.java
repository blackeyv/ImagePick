package com.raymond.imagepick.adapter;

import java.util.ArrayList;

import com.raymond.imagepick.activity.ImageViewActivity;
import com.raymond.imagepick.object.LocalImageLoader;
import com.raymond.imagepick.object.PhotoWallImageView;
import com.raymond.imagepick.object.ScreenSizeUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.support.v4.view.PagerAdapter;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * ViewPager的PagerAdapter
 * @author Raymond Yan
 *
 */
public class ViewPagerAdapter extends PagerAdapter
{
	private ArrayList<String> m_imagePaths;
	private Context m_ctx;
	private int m_screenWidth;
	private int m_screenHeight;
	private PhotoWallImageView m_imageView;
	
	public ViewPagerAdapter (ArrayList<String> imagePaths, Context ctx)
	{
		m_imagePaths = imagePaths;
		m_ctx = ctx;
		
        m_screenWidth = ScreenSizeUtil.getScreenW();	// 屏幕宽（像素，如：480px)
        m_screenHeight = ScreenSizeUtil.getScreenH();
	}
	
	@Override
	public int getCount() 
	{
		return m_imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) 
	{
		return view == (View) object; 
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) 
	{
		container.removeView((View)object);
		
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int position) 
	{
		m_imageView = new PhotoWallImageView(m_ctx); //new Custom ImageView
		m_imageView.setSingleTapHide(); //设置模式为 单击隐藏 actionbar和snackbar
        try { 
        	//异步加载图片，压缩率为屏幕size的1/2
        	Bitmap bmp = LocalImageLoader.CompressImage(m_imagePaths.get(position), 2, m_screenWidth, m_screenHeight);
        	
    		if (bmp != null) 
    		{
    			m_imageView.setImageBitmap(bmp);
    		}
    		else
    		{
    			return null;
    		}
    		
        } catch (OutOfMemoryError e) {  
            // TODO Auto-generated catch block  
            e.printStackTrace();  
        }
        container.addView(m_imageView);
        return m_imageView;  
	}
}
