package com.raymond.imagepick.activity;

import java.util.ArrayList;

import com.raymond.imagepick.R;
import com.raymond.imagepick.adapter.ViewPagerAdapter;
import com.raymond.imagepick.object.SparseBooleanArrayParcelable;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.RelativeLayout;


public class ImageViewActivity extends AppCompatActivity
{
	/** 存储已选择图片的数目，生成显示类似  1/9 的格式 */
	private MenuItem m_CountTitle;
	/**
	 * 存放当前文件夹下面所有图片的路径
	 */
	private ArrayList<String> m_imagePaths; 
	/**
	 * 已选择的图片的本地路径。
	 */
	private ArrayList<String> m_selectedPaths;
	/**
	 * 现在选择的图片在m_imagePaths中的index位置。
	 */
	private int m_position;
	
	/**
	 * 现在已经选择的图片数量，最大为9
	 */
	private int m_selectedNum;
	/**
	 * layout下方的状态栏，用于选择图片。
	 */
	private RelativeLayout m_snackBar;
	/**
	 * 显示当前图片是否已经选择
	 */
	private CheckBox m_selectCB;
	/**
	 * 实际上选择图片的button，比checkbox的范围大，方便选择图片。因为checkbox的大小太小不容易选上。
	 */
	private Button m_selectBtn;
	/**
	 * 记录所有已选择的图片在m_imagePaths的index位置
	 */
	private SparseBooleanArrayParcelable m_selectionMap; 
	
	/**
	 * custom PagerAdapter，存储当前所有的图片，方便左右滑动。
	 */
	private ViewPagerAdapter m_adapter;
	private ViewPager m_viewPager;
	/**
	 * 按返回键返回时的 resultcode
	 */
	public static final int IMAGEVIEW_BACK = 100;
	/**
	 * 按发送键返回时的 resultcode
	 */
	public static final int IMAGEVIEW_SEND = 101;
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
    {
		supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable( new ColorDrawable(getResources().getColor(R.color.bg_title)) );
        getSupportActionBar().setTitle( getIntent().getStringExtra("title") );
        initUI();
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.menu_image_select, menu);
		super.onCreateOptionsMenu(menu);
		//初始化 右上角的 menuitem。
		m_CountTitle = menu.findItem(R.id.image_select_confirm);
		// 设置已选的图片数量。
		setCount();
		return true;
	}
	/**
	 * 设置已选择的图片的数目（m_selectedNum），格式为 n/9 ，最大9张。 
	 */
	private void setCount()
	{
		if(m_selectedNum>0)
		{
			m_CountTitle.setTitle(getResources().getString(R.string.str_image_send)+"("+m_selectedNum+"/"+"9)");
		}
		else
		{
			m_CountTitle.setTitle(getResources().getString(R.string.str_image_send));
		}
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			// 按actionbar的返回键。
			case android.R.id.home: 
				onBackPressed();
				return true;
			// 按menu Item 的发送
			case R.id.image_select_confirm:
				Intent intent =sendBackData();
				setResult(IMAGEVIEW_SEND,intent);
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
    }
	
	@Override
	public void onBackPressed()
	{
		Intent intent = sendBackData();
		setResult(IMAGEVIEW_BACK, intent);
		super.onBackPressed();
	}
	
	/**
	 * 送回前一个activity 选择的图片的index 和 path；
	 * @return
	 */
	private Intent sendBackData()
	{
		Intent intent = new Intent();
		intent.putExtra("selectednum", m_selectedNum);
		intent.putStringArrayListExtra("selectedpaths",m_selectedPaths);
		intent.putExtra("selectionmap", m_selectionMap);
		return intent;
	}
	/**
	 * initUI。
	 */
	private void initUI()
	{
		m_imagePaths = getIntent().getStringArrayListExtra("filepaths"); //获得文件夹里面所有图片的地址
		m_selectedNum = getIntent().getIntExtra("selectednum", 0); //获得已选择的图片的个数
		m_selectedPaths = getIntent().getStringArrayListExtra("selectedpaths");  //已选择的图片的路径
		m_selectionMap = (SparseBooleanArrayParcelable) getIntent().getExtras().get("selectionmap"); //已选择图片的position。
		m_snackBar = (RelativeLayout) findViewById(R.id.image_select_snackbar);
		m_snackBar.setVisibility(View.VISIBLE);
		m_selectCB = (CheckBox) findViewById(R.id.image_select_checkbox);  
		m_selectBtn = (Button) findViewById(R.id.image_select_snackbar_click);
		m_position = getIntent().getIntExtra("position", 0);  //获得当前图片在m_imagePaths里面的index
		
		String title = String.valueOf(m_position+1)+"/"+String.valueOf(m_imagePaths.size()); //显示title为1/100 样式。
		getSupportActionBar().setTitle(title);
		
		m_adapter = new ViewPagerAdapter(m_imagePaths, ImageViewActivity.this); //设置 viewpager的adpater
		m_viewPager =  (ViewPager) findViewById(R.id.image_select_view_pager);
		m_viewPager.setAdapter(m_adapter);
		m_viewPager.setCurrentItem(m_position); //设置当前图片的index
		m_viewPager.addOnPageChangeListener(new onImageSwitchListener()); //切换pager时的listener。
		
		m_selectBtn.setOnClickListener(new onImageSelect());
	}
	
	/**
	 * 单击 隐藏/显示  actionbar和下方的状态栏
	 */
	public void onSingleTap()
	{
		if(getSupportActionBar().isShowing())
		{
			m_snackBar.setVisibility(View.GONE);
			getSupportActionBar().hide();
		}
		else
		{
			m_snackBar.setVisibility(View.VISIBLE);
			getSupportActionBar().show();
		}
	}
	
	/**
	 * 切换imagepage图片时的listener
	 * @author Raymond Yan
	 *
	 */
	private class onImageSwitchListener implements OnPageChangeListener
	{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) 
		{
			// TODO Auto-generated method stub
			Log.d("raymond","position=="+arg0+"arg1"+arg1+"arg2"+arg2);
			m_position = arg0;  //arg0为pagescrolled后的position。
			String title = String.valueOf(m_position+1)+"/"+String.valueOf(m_imagePaths.size()); //显示title 1/100 样式。
			getSupportActionBar().setTitle(title);
			if(m_selectionMap.get(m_position))  //查询SparseBooleanArray里面存储的index和boolean值，是否已经选择过该图片，如果有选择过，则将checkbox设置为checked。
			{
				m_selectCB.setChecked(true);
			}
			else
			{
				m_selectCB.setChecked(false);
			}
		}

		@Override
		public void onPageSelected(int arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	/**
	 * 透明的button的onclickListener，因为原本checkbox的太小，不好按。
	 * @author Raymond Yan
	 *
	 */
	private class onImageSelect implements Button.OnClickListener
	{
		@Override
		public void onClick(View v) 
		{
			if(m_selectCB.isChecked())
			{
				m_selectedPaths.remove(m_imagePaths.get(m_position)); //清除已选择的图片路径
				m_selectedNum--;  //选择的数目-1
				m_selectCB.setChecked(false);  //setchecked false
				m_selectionMap.put(m_position, false);  //selectionmap 标记
			}
			else
			{
				if(m_selectedNum >=9 ) //如果数目大于等于9
				{
					Toast.makeText(getApplicationContext(), R.string.str_image_send_full, Toast.LENGTH_SHORT).show();
					return;
				}
				m_selectedPaths.add(m_imagePaths.get(m_position)); //增加已选图片的路径
				m_selectedNum++; //数目+1
				m_selectCB.setChecked(true);
				m_selectionMap.put(m_position, true);
			}
			setCount();
		}
		
	}
}