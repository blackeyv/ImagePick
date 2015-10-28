package com.raymond.imagepick.object;

import com.raymond.imagepick.activity.ImageViewActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class PhotoWallImageView extends ImageView
{

	/** 记录是拖动照片模式还是放大缩小模式 */
	private int mode = 0;// 初始狀態  
	/** 拖拉照片模式 */
	private static final int MODE_DRAG = 1;
	/** 放大縮小照片模式 */
	private static final int MODE_ZOOM = 2;
	
	/** 用於記錄開始時候的坐標位置 */
	private PointF startPoint = new PointF();
	/** 用於記錄拖拉圖片移動的坐標位置 */
	private Matrix matrix = new Matrix();
	/** 用於記錄圖片要進行拖拉時候的坐標位置 */
	private Matrix currentMatrix = new Matrix();

	/** 兩個手指的開始距離 */
	private float startDis;
	/** 兩個手指的中間點 */
	private PointF midPoint;
	
	private int bmpWidth;
	private int bmpHeight;
	private Matrix originMatrix = new Matrix();
	private float[] originValues;
	
	private int m_screenWidth;  //屏幕宽度
	private int m_screenHeight; //屏幕高度
	
	private Context m_ctx;
	
	private static final int SINGLE_TAP_HIDE = 100; //设置模式为单击隐藏actionbar和snackbar
	private static final int SINGLE_TAP_DISMISS = 101; //单击dismiss图片
	private int SINGLE_TAP_MODE = 0; //单击时的模式。
	
	
	public PhotoWallImageView(Context context) {
		super(context);
		
        m_screenWidth = ScreenSizeUtil.getScreenW();	// 屏幕宽（像素，如：480px)
        m_screenHeight = ScreenSizeUtil.getScreenH();
        m_ctx = context;
		// TODO Auto-generated constructor stub
        this.setOnClickListener(new OnClickListener()  //必须要设置了 onclicklistener 下面的onTouchEvent才能够被触发。 原因未知..
        {
			@Override
			public void onClick(View v) 
			{
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	/**
	 * ImageView的 onTouchEvent，实现图片拖动，放大，缩小等
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) 
	{
		// TODO Auto-generated method stub
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
			// 手指壓下屏幕
			case MotionEvent.ACTION_DOWN:
				
				// 記錄ImageView當前的移動位置
				currentMatrix.set(this.getImageMatrix());
				matrix.set(currentMatrix);
				if(checkSize())  //如果图片没有被放大，就允许viewpager翻页动作
				{
					this.getParent().requestDisallowInterceptTouchEvent(false); //允许viewpager动作
				}
				else
				{
					this.getParent().requestDisallowInterceptTouchEvent(true); //禁止viewpager的翻页动作
				}
				startPoint.set(event.getX(), event.getY());
				mode = MODE_DRAG;
				break;
			// 手指在屏幕上移動，事件會被不斷觸發
			case MotionEvent.ACTION_MOVE:
				// 拖拉圖片
				if (mode == MODE_DRAG) 
				{
					float[] values=new float[9];
					matrix.getValues(values);
					
					//如果图片没有被缩小，则禁止拖动
					if(checkSize())
					{
						break;
					}
					float dx = event.getX() - startPoint.x; // 得到x軸的移動距離
					float dy = event.getY() - startPoint.y; // 得到y軸的移動距離
					// 在沒有移動之前的位置上進行移動
					matrix.set(currentMatrix);
					matrix.postTranslate(dx, dy);
					this.setImageMatrix(matrix);
				}
				// 放大縮小圖片
				else if (mode == MODE_ZOOM) {
					this.getParent().requestDisallowInterceptTouchEvent(true); //禁止viewpager动作
					float endDis = distance(event);// 結束距離
					if (endDis > 10f) { // 兩個手指並攏在一起的時候像素大於10
						float scale = endDis / startDis;// 得到縮放倍數
						matrix.set(currentMatrix);
						matrix.postScale(scale, scale,midPoint.x,midPoint.y);
					}
				}
				break;
			// 手指離開屏幕
			case MotionEvent.ACTION_UP:
			{
				float[] values=new float[9];
				matrix.getValues(values);
				
				if(checkSize()) //缩小倍数是否超过原来的大小
				{
					
					matrix.set(originMatrix);
					this.setImageMatrix(matrix);
					mode = 0;
					
					values=new float[9];
					matrix.getValues(values);
					
					Log.d("raymond","AFTER :   values[Matrix.MSCALE_X]="+values[Matrix.MSCALE_X]+"originValues[Matrix.MSCALE_X]"+originValues[Matrix.MSCALE_X]);
					gestureDetector.onTouchEvent(event);
					return super.onTouchEvent(event);
				}
				else
				{
					//检查拖动后图片是否已经超出边界
					checkDragable();
				}
			}
				// 當觸點離開屏幕，但是屏幕上還有觸點(手指)
			case MotionEvent.ACTION_POINTER_UP:
				mode = 0;
				break;
			// 當屏幕上已經有觸點(手指)，再有一個觸點壓下屏幕
			case MotionEvent.ACTION_POINTER_DOWN:
				mode = MODE_ZOOM;
				/** 計算兩個手指間的距離 */
				startDis = distance(event);
				/** 計算兩個手指間的中間點 */
				if (startDis > 10f) { // 兩個手指並攏在一起的時候像素大於10
					midPoint = mid(event);
					//記錄當前ImageView的縮放倍數
					currentMatrix.set(this.getImageMatrix());
				}
				break;
			}
		this.setImageMatrix(matrix);
		gestureDetector.onTouchEvent(event);  //用gestureDetector 来判断 double tap , long press, single tap 等事件。
		return super.onTouchEvent(event);
	}
	
	/**
	 * 检查图片是否有被放大，没有放大图片时返回为true，有放大图片返回为false
	 */
	private boolean checkSize()
	{
		float[] values=new float[9];
		matrix.getValues(values);
		
		return (values[Matrix.MSCALE_X] <= originValues[Matrix.MSCALE_X] 
				|| values[Matrix.MSCALE_Y] <= originValues[Matrix.MSCALE_Y]); //缩小倍数是否超过原来
	}
	
	/** 計算兩個手指間的距離 */
	private float distance(MotionEvent event) {
		float dx = event.getX(1) - event.getX(0);
		float dy = event.getY(1) - event.getY(0);
		/** 使用勾股定理返回兩點之間的距離 */
		return FloatMath.sqrt(dx * dx + dy * dy);
	}

	/** 計算兩個手指間的中間點 */
	private PointF mid(MotionEvent event) {
		float midX = (event.getX(1) + event.getX(0)) / 2;
		float midY = (event.getY(1) + event.getY(0)) / 2;
		return new PointF(midX, midY);
	}
	/**
	 * 检查图片是否有超出边界
	 */
	private void checkDragable() {
		
		/** 获得图片拖动后的四个角的坐标，具体方式参照Matrix的计算公式*/
		
        float[] values=new float[9];
        matrix.getValues(values);
        int TopLeftX=(int) values[Matrix.MTRANS_X];   //左上角坐标x
        int TopLeftY=(int) values[Matrix.MTRANS_Y];		//左上角坐标y
        float BtmRightX =  ((bmpWidth)*values[Matrix.MSCALE_X]+values[Matrix.MTRANS_X]); //右下角坐标x
        float BtmRightY =  (values[Matrix.MTRANS_Y]+bmpHeight*values[Matrix.MSCALE_Y]);   //右下角坐标y
        
        Log.d("raymond", "左上角坐标：x "  + values[Matrix.MTRANS_X]+"   y "+values[Matrix.MTRANS_Y]);
        Log.d("raymond", "右下角坐标：x "  + BtmRightX+"   y "+BtmRightY);
        Log.d("raymond","ImageView x："+this.getWidth()+"Y="+this.getHeight());
        
        /** 先获得图片原来四个角的坐标，具体方式参照Matrix的计算公式*/
        
        int OldTopLeftX=(int) originValues[Matrix.MTRANS_X];   //原左上角坐标x
        int OldTopLeftY=(int) originValues[Matrix.MTRANS_Y];		//原左上角坐标y
        float OldBtmRightX =  (bmpWidth*originValues[Matrix.MSCALE_X]+originValues[Matrix.MTRANS_X]); //原右下角坐标x
        float OldBtmRightY =  (originValues[Matrix.MTRANS_Y]+bmpHeight*originValues[Matrix.MSCALE_Y]);   //原右下角坐标y
        
        Log.d("raymond", "原左上角坐标：x "  + OldTopLeftX+"   y "+OldTopLeftY);
        Log.d("raymond", "原右下角坐标：x "  + OldBtmRightX+"   y "+OldBtmRightY);
        Log.d("raymond","ImageView x："+this.getWidth()+"Y="+this.getHeight());
        
        
        //图片左边缘离开左边界，左边复原
        if( TopLeftX >= OldTopLeftX )
        	matrix.postTranslate(-TopLeftX, 0);
        //图片右边缘离开右边界，右边复原
        if(BtmRightX < OldBtmRightX){
        	matrix.postTranslate(OldBtmRightX-BtmRightX, 0);
        }
        
        if( TopLeftY >= OldTopLeftY)  //上边界复原
        {
        	matrix.postTranslate( 0, -TopLeftY );
        }
        
        if( BtmRightY < OldBtmRightY) // 下边界复原
        {
        	matrix.postTranslate( 0, OldBtmRightY-BtmRightY );
        }
    }
	
	/**
	 * 检测单击，双击图片的事件，辅助ontouchevent判断。
	 */
	@SuppressWarnings("deprecation")
	final private GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener()  //检测是否为单击，双击
	{
		
		/**
		 * 双击图片 放大图片，或者返回初始大小
		 * @param e
		 * @return
		 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
	    	float[] values=new float[9];
			matrix.getValues(values);
			
			if(!checkSize()) //图片如果有放大，则缩回初始大小
			{
				matrix.set(originMatrix);
			}
			else // 图片没有放大则放大
			{
				float scale = (float) 1.5;
				matrix.postScale(scale, scale, e.getX(), e.getY());
				matrix.getValues(values);
			}
			mode = 0;
			PhotoWallImageView.this.setImageMatrix(matrix);
			return super.onDoubleTap(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
	    	
			return super.onSingleTapUp(e);
		}
		
		/**
		 * 单击图片隐藏 显示 actionbar 
		 */
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) 
		{
			if(SINGLE_TAP_MODE == SINGLE_TAP_HIDE)   //如果模式是单击隐藏actionbar
			{
				try 
				{
					((ImageViewActivity)m_ctx).onSingleTap();
				} catch (Exception e2) {
					e2.printStackTrace();
					// TODO: handle exception
				}
			}
			
			return super.onSingleTapConfirmed(e);
		}

		public void onLongPress(MotionEvent e) 
	    {
	        Log.e("raymond", "Longpress detected");
	    }
	});
	
	/**
	 * 从image path获得BMP，压缩为屏幕分辨率的1/2后，显示出来。
	 * @param path
	 */
	public void setImagePath(String path)
	{
		Bitmap bmp = LocalImageLoader.CompressImage(path, 2, m_screenWidth, m_screenHeight);
    	
		if (bmp != null) 
		{
			this.setImageBitmap(bmp);
		}
		
	}
	
	/**
	 * 设置单击时的响应为 隐藏actionbar和snackbar
	 */
	public void setSingleTapHide()
	{
		SINGLE_TAP_MODE = SINGLE_TAP_HIDE;
	}
	
	public void setSingleTapDismiss()
	{
		SINGLE_TAP_MODE  = SINGLE_TAP_DISMISS;
	}
	
	@Override
	public void setImageBitmap(Bitmap bmp) {
		// TODO Auto-generated method stub
		
		this.setScaleType(ScaleType.MATRIX);
		 //设置scaletype 为 matrix，这样下面才能对其设置matrix。默认为 fitcenter。
		Matrix matrix = this.getImageMatrix();
		/**将图片的大小缩放为适合屏幕的大小，实现的效果跟 ScaleToFit.CENTER 一样。但是必须设置为 setScaleType(ScaleType.MATRIX) 才可以通过
		 * setmatrix 来缩放图片。因此需要先手动实现ScaleToFit.CENTER的效果  */
		RectF drawableRect = new RectF(0, 0, bmp.getWidth(), bmp.getHeight());
	    RectF viewRect = new RectF(0, 0, m_screenWidth, m_screenHeight);
	    matrix.setRectToRect(drawableRect, viewRect, Matrix.ScaleToFit.CENTER);
	    this.setImageMatrix(matrix);
		
	    /** 存储用户没有缩放时的图片的初始数据 */
	    originValues=new float[9];
		matrix.getValues(originValues);
		this.bmpWidth = bmp.getWidth();
		this.bmpHeight = bmp.getHeight();
		this.originMatrix.set(matrix);
	    
		super.setImageBitmap(bmp);
	}
	
	

}


