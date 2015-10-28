package com.raymond.imagepick.object;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.raymond.imagepick.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.widget.ImageView;


/**
 *  异步加载本地图片
 * @author Raymond Yan
 *
 */
public class LocalImageLoader 
{
	/**
	 * 缓存
	 */
    private LruCache<String, Bitmap> m_imageCache;
    /**
     * 固定只开2个线程来执行任务
     */
    private ExecutorService m_executorService = Executors.newFixedThreadPool(2);
    /**
     * handler回到主线程显示图片
     */
    private Handler m_handler = new Handler();

    private int m_screenW, m_screenH;
    
    public LocalImageLoader(int width, int height)
    {
    	this.m_screenH = height;
    	this.m_screenW = width;
    	
    	 // 获取应用程序最大可用内存
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 8;
        // 设置图片缓存大小为程序最大可用内存的1/8
        m_imageCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight();
            }
        };
    }
    
    private Bitmap loadDrawable (final int compressRate, final String filePath,
            final ImageCallback callback) 
    {
    	//先查询 在cache里有没bitmap
    	if( m_imageCache.get(filePath) != null)
    	{
    		return m_imageCache.get(filePath);
    	}
    	
    	 // 如果没有则读取SD卡
        m_executorService.submit(new Runnable() 
        {
            public void run() 
            {
                try 
                {
                    final Bitmap bmp = CompressImage(filePath, compressRate,m_screenW,m_screenH);
                    //存入map
                    m_imageCache.put(filePath, bmp);

                    m_handler.post(new Runnable()  //load 完image之后
                    {
                        public void run() 
                        {
                            callback.imageLoaded(bmp);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    	
		return null;
    	
    }
    /**
     * 异步读取SD卡图片，并按指定的比例进行压缩（最大不超过屏幕像素数）
     *  
     * @param smallRate 压缩比例，不压缩时输入1，此时将按屏幕像素数进行输出
     * @param filePath  图片在SD卡的全路径
     * @param imageView 组件
     */
    
    public void loadImage(int compressRate, final String filePath, final ImageView imageView) {

        Bitmap bmp = loadDrawable(compressRate, filePath, new ImageCallback() {
        	// 当cache里面没有bitmap，在load完了image的时候，通过回传的接口来设置imageview，需要比对 imageview的TAG。
            @Override
            public void imageLoaded(Bitmap bmp) {
                if (imageView.getTag().equals(filePath)) {
                    if (bmp != null) {
                        imageView.setImageBitmap(bmp);
                    } else {
                        imageView.setImageResource(R.drawable.empty_photo);
                    }
                }
            }
        });
        //当存在cache的时候。
        if (bmp != null) {
            if (imageView.getTag().equals(filePath)) {
                imageView.setImageBitmap(bmp);
            }
        } else {
            imageView.setImageResource(R.drawable.empty_photo);
        }

    }
    
    /**
     *  用于通知image已经load完的callback接口，在这里将load完的bitmap添加到对应的imageview上。
     * @author Raymond Yan
     *
     */
    public interface ImageCallback {
     
        public void imageLoaded(Bitmap imageDrawable);
    }

    
    /**
     *  
     * @param filePath  图片文件的绝对路径
     * @param compressRate 压缩率（压缩为屏幕分比率的几分之一
     * @param screenW 屏幕宽度
     * @param screenH 屏幕高度
     * @return
     */
    
    public static Bitmap CompressImage (String filePath, int compressRate, int screenW, int screenH)
    {
    	BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true; //这样decodefile的时候可以拿到bitmap的参数，而不用分配内存来存放bitmap的像素。
        BitmapFactory.decodeFile(filePath, opt);

        // 获取到这个图片的原始宽度和高度
        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        //读取图片失败时直接返回
        if (picWidth == 0 || picHeight == 0) {
            return null;
        }

        //初始压缩比例
        opt.inSampleSize = compressRate;
        // 根据屏的大小和图片大小计算出缩放比例, 如果 图片像素大小大于屏幕，则压缩率乘以 图片宽度除以屏幕宽度，或者 图片长度除以屏幕长度。
        // 这样压缩的比率就是屏幕大小的*设定的smallRate，这样可以根据不同的屏幕拿到不同压缩率的缩略图来显示，保证图片压缩率和清晰度。
        if (picWidth > picHeight) 
        {
            if (picWidth > screenW)
                opt.inSampleSize *= picWidth / screenW;
        } 
        else 
        {
            if (picHeight > screenH)
                opt.inSampleSize *= picHeight / screenH;
        }

        //这次再真正地生成一个有像素的，经过缩放了的bitmap
        opt.inJustDecodeBounds = false;   //set false，decode的时候就会真正生成bitmap。
        File f = new File(filePath);
        InputStream inputStream = null;
		try {
			inputStream = new BufferedInputStream( new FileInputStream( f.toString() ) );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return BitmapFactory.decodeStream(inputStream,null,opt);
    }
}
    
