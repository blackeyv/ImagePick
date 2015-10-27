package com.raymond.imagepick.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import com.raymond.imagepick.R;
import com.raymond.imagepick.adapter.ImageParentPathAdapter;
import com.raymond.imagepick.adapter.ImageSelectAdapter;
import com.raymond.imagepick.object.ImageParentPathItem;
import com.raymond.imagepick.object.ImagePickResponse;
import com.raymond.imagepick.object.ScreenSizeUtil;
import com.raymond.imagepick.object.SparseBooleanArrayParcelable;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.Toast;


public class ImageSelectActivity extends AppCompatActivity
{
	/** 存储已选择图片的数目，生成显示类似  1/9 的格式 */
	private MenuItem m_CountTitle;
	
	private ImageSelectAdapter m_adapter;
	final static private int SHOWIMAGE = 1000;
	/**
	 * 存放当前文件夹下面所有图片的路径
	 */
	private ArrayList<String> m_imagePaths;  
	/**
	 * 选择图片文件夹
	 */
	private Spinner m_fileSpinner; 
	/**
	 * 存取文件夹路径，路径，图片数目；
	 */
	private ArrayList<ImageParentPathItem> m_spinnerArray; 
	/**
	 * custom spinner adapter，显示照片文件夹的名字，第一张图片以及图片的数量。
	 */

	private ImageParentPathAdapter m_spinnerAdapter; //
	
	/**
	 * boolean值储存为用户与手机的交互动作
	 */
	private boolean m_userIsInteracting = false;
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_image_select, menu);
		super.onCreateOptionsMenu(menu);
		m_CountTitle = menu.findItem(R.id.image_select_confirm);
		return true;
	}
    
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable( new ColorDrawable(getResources().getColor(R.color.bg_title)) );
        getSupportActionBar().setTitle( getResources().getString(R.string.str_image_send_recent) ); //设置title为最近照片
        m_imagePaths = getLatestImagePaths(100); //读取最近100张照片
        ScreenSizeUtil.initScreen(ImageSelectActivity.this);
        initUI();
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case android.R.id.home: 
				onBackPressed();
				return true;
			case R.id.image_select_confirm:
				if(m_adapter.getNum() == 0)
				{
					Toast.makeText(getApplicationContext(), R.string.str_image_send_null, Toast.LENGTH_SHORT).show();
					return true;
				}
				sendImage();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
    }
	
	/**
	 * 发送图片
	 */
	private void sendImage()
	{
		ArrayList<String> filePaths = m_adapter.getSelectedFilePath();
		Intent intent = new Intent();
		intent.putStringArrayListExtra("filepaths", filePaths);
		setResult(RESULT_OK,intent);
		onBackPressed();
	}
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
	}
	
	private void initUI()
	{	
		
        GridView gridview = (GridView) findViewById(R.id.image_gridview); 
        m_adapter = new ImageSelectAdapter(ImageSelectActivity.this, m_imagePaths, new ImageCheck()); //初始化gridview的adapter
        gridview.setAdapter(m_adapter); 
        
        setFileSelectSpinner();
	}
	/**
	 * 设置页面左下角的spinner，读取有存照片的文件夹，并且显示在上拉菜单中
	 */
	private void setFileSelectSpinner()
	{
		m_fileSpinner = (Spinner) findViewById(R.id.image_select_files_spinner); //定义 spinner
		
		m_spinnerArray = new ArrayList<ImageParentPathItem>(); 
		m_spinnerArray.add(new ImageParentPathItem(getResources().getString(R.string.str_image_send_recent),
                m_imagePaths.size(), m_imagePaths.get(0))); //先加入 最近照片(100张） 这一文件夹。
		m_spinnerArray.addAll(getImagePathsByContentProvider()); //获得存储照片的文件夹 路径。
		m_spinnerAdapter = new ImageParentPathAdapter(ImageSelectActivity.this, R.layout.list_item_filepath, m_spinnerArray);
		m_fileSpinner.setAdapter(m_spinnerAdapter);
		m_fileSpinner.setOnItemSelectedListener(new onSpinnerClickListener());
	}
	
	public class onSpinnerClickListener implements OnItemSelectedListener
	{

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) 
		{
			// 因为在程序开始运行的时候，会默认用户已经选择了spinnerAdapter里面的第一个默认选项，会执行 onItemSelected（）一次，
			// 因此需要查看是否为用户的点击选择或者是程序运行时的默认值。
			if(m_userIsInteracting)   
			{
				String title = m_spinnerAdapter.getPathNameToShow(m_spinnerArray.get(position).getPathName());
				if(getSupportActionBar().getTitle().equals(title))  //如果所选的图片文件夹与当前文件夹名字一样，则返回
				{
					return;
				}
				getSupportActionBar().setTitle(title);   //显示所选文件夹的名字
				m_imagePaths.clear();  //清除存储的前一个文件夹
				if(position == 0)
				{
					m_imagePaths.addAll(getLatestImagePaths(100)); //载入最近的100张照片
				}
				else
				{
					m_imagePaths.addAll(getAllImagePathsByFolder(m_spinnerArray.get(position).getPathName())); //载入所选其他文件夹照片
				}
				m_adapter.ClearAll();
				setCount(0);
				m_adapter.notifyDataSetChanged();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 * 因为第一次ini的时候，spinner 会把adapter 第一个设置为默认，然后就会触发OnItemSelectedListener，默认选择了第一个、所以要判断是否由用户interaction触发。
	 */
	@Override
	public void onUserInteraction()  
	{
	     super.onUserInteraction();
	     m_userIsInteracting = true;
	}
	
	/**
	 * 由 ImageAdapter 里面的callback回来的值，选择了图片或者取消选择，在activity更新UI显示选择的个数。
	 */
	public class ImageCheck implements ImagePickResponse
	{
		
		@Override
		public void onImagePicked(int num) 
		{
			/*int num = getSelectedNum();*/
			setCount(num);
		}

		@Override
		public void onImageUnpicked(int num) 
		{
			/*int num = getSelectedNum();*/
			setCount(num);
		}

		@Override
		public void onFull() 
		{
			Toast.makeText(getApplicationContext(), R.string.str_image_send_full, Toast.LENGTH_SHORT).show();
		}
		
		/**
		 *  在ImageSelectAdapter中调用，显示用户所选的图片，进入ImageViewActivity
		 * @param position 在m_imagePaths的position
		 */
		@Override
		public void onImageShow( int position ) 
		{
			// TODO Auto-generated method stub
			Intent intent = new Intent(ImageSelectActivity.this,ImageViewActivity.class);
			intent.putStringArrayListExtra("filepaths", m_imagePaths);
			intent.putExtra("position", position);
			intent.putExtra("selectednum", m_adapter.getNum());
			intent.putStringArrayListExtra("selectedpaths",m_adapter.getSelectedFilePath());
			intent.putExtra("selectionmap", m_adapter.getSelectionMap());
			startActivityForResult(intent, SHOWIMAGE);
		}
	}
	/**
	 *  设置标题显示的
	 * @param num 已选的图片数目，类似 2/9
	 */
	private void setCount(int num)
	{
		if(num>0)
		{
			m_CountTitle.setTitle(getResources().getString(R.string.str_image_send)+"("+num+"/"+"9)");
		}
		else
		{
			m_CountTitle.setTitle(getResources().getString(R.string.str_image_send));
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
	{
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		if(requestCode == SHOWIMAGE)
		{
			if(resultCode == RESULT_CANCELED)
			{
				return;
			}
			//在ImageViewActivity按返回键返回时
			if(resultCode == ImageViewActivity.IMAGEVIEW_BACK)
			{
				int num = intent.getIntExtra("selectednum", 1);
				ArrayList<String> selectedpath = intent.getStringArrayListExtra("selectedpaths");
				SparseBooleanArrayParcelable array = (SparseBooleanArrayParcelable) intent.getExtras().get("selectionmap");
				m_adapter.setNum(num);
				m_adapter.setSelectedFilePath(selectedpath);
				m_adapter.setSelectionMap(array);
				m_adapter.notifyDataSetChanged();
				setCount(num);
			}
			//在ImageViewActivity按发送键返回时。
			else if (resultCode == ImageViewActivity.IMAGEVIEW_SEND)
			{
				ArrayList<String> selectedpath = intent.getStringArrayListExtra("selectedpaths");
				m_adapter.setSelectedFilePath(selectedpath);
				sendImage();
			}
		}
	}
	
	/**
	 * 获取最近的 100 张照片，按照日期排序读取。
	 * @param maxCount
	 * @return
	 */
	private ArrayList<String> getLatestImagePaths(int maxCount) 
	{
		Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
		String key_DATA = MediaStore.Images.Media.DATA;

		ContentResolver mContentResolver = getContentResolver();

		// 只查询jpg和png的图片,按最新修改排序
		Cursor cursor = mContentResolver.query(mImageUri,
				new String[] { key_DATA }, key_MIME_TYPE + "=? or "
						+ key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
				new String[] { "image/jpg", "image/jpeg", "image/png" },
				MediaStore.Images.Media.DATE_MODIFIED);

		ArrayList<String> latestImagePaths = null;
		if (cursor != null) {
            //从最新的图片开始读取.
            //当cursor中没有数据时，cursor.moveToLast()将返回false
            if (cursor.moveToLast()) 
            {
                latestImagePaths = new ArrayList<String>();

                do
                {
                    // 获取图片的路径
                    String path = cursor.getString(0);
                    latestImagePaths.add(path);
                }while( cursor.moveToPrevious() && latestImagePaths.size() < maxCount);
            }
            cursor.close();
        }
		
		return latestImagePaths;
	}
	
	/**
     * 获取指定路径下的所有图片文件。
     */
    private ArrayList<String> getAllImagePathsByFolder(String folderPath) {
        File folder = new File(folderPath);
        String[] allFileNames = folder.list();
        if (allFileNames == null || allFileNames.length == 0) {
            return null;
        }

        ArrayList<String> imageFilePaths = new ArrayList<String>();
        for (int i = allFileNames.length - 1; i >= 0; i--) {
            if (isImage(allFileNames[i])) {
                imageFilePaths.add(folderPath + File.separator + allFileNames[i]);
            }
        }

        return imageFilePaths;
    }
	
    /**
     * 返回MediaStore.Images.Media.EXTERNAL_CONTENT_URI 里的 图片的文件夹的名字，数目以及第一张图片的路径。
     * @return  ArrayList<ImageParentPathItem>， 
     */
	 private ArrayList<ImageParentPathItem> getImagePathsByContentProvider() 
	 {
	        Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

	        String key_MIME_TYPE = MediaStore.Images.Media.MIME_TYPE;
	        String key_DATA = MediaStore.Images.Media.DATA;

	        ContentResolver mContentResolver = getContentResolver();
	        
	        // 只查询jpg和png的图片
	        Cursor cursor = mContentResolver.query(mImageUri, new String[]{key_DATA},
	                key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=? or " + key_MIME_TYPE + "=?",
	                new String[]{"image/jpg", "image/jpeg", "image/png"},
	                MediaStore.Images.Media.DATE_MODIFIED);
	        ArrayList<ImageParentPathItem> arraylist = new ArrayList<ImageParentPathItem>();
	        /*ArrayList<PhotoAlbumLVItem> list = null;*/
	        if (cursor != null) {
	            if (cursor.moveToLast()) {
	                //路径缓存，防止多次扫描同一目录
	                HashSet<String> cachePath = new HashSet<String>();
	                /*list = new ArrayList<PhotoAlbumLVItem>();*/

	               do {
	                    // 获取图片的路径
	                    String imagePath = cursor.getString(0);

	                    File parentFile = new File(imagePath).getParentFile();
	                    String parentPath = parentFile.getAbsolutePath();

	                    //不扫描重复路径
	                    if (!cachePath.contains(parentPath)) 
	                    {
	                        Log.d("raymond", "parentPath"+parentPath);
	                        cachePath.add(parentPath);
	                        ImageParentPathItem item = new ImageParentPathItem(parentPath, getImageCount(parentFile), getFirstImagePath(parentFile));
	                        arraylist.add(item);
	                    }
	                } while (cursor.moveToPrevious());
	            }
	            cursor.close();
	        }
			return arraylist;
	    }
	 
	  /**
	     * 获取目录中图片的个数。
	     */
	    private int getImageCount(File folder) {
	        int count = 0;
	        File[] files = folder.listFiles();
	        for (File file : files) {
	            if (isImage(file.getName())) {
	                count++;
	            }
	        }

	        return count;
	    }

	    /**
	     * 获取目录中最新的一张图片的绝对路径。
	     */
	    private String getFirstImagePath(File folder) {
	        File[] files = folder.listFiles();
	        for (int i = files.length - 1; i >= 0; i--) {
	            File file = files[i];
	            if (isImage(file.getName())) {
	                return file.getAbsolutePath();
	            }
	        }

	        return null;
	    }
	    /**
	     * 查询是否为图片文件
	     * @param fileName
	     * @return
	     */
	    private boolean isImage(String fileName) 
	    {
	        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
	    }

}

