package com.raymond.imagepick.adapter;

import java.util.ArrayList;

import com.raymond.imagepick.R;
import com.raymond.imagepick.object.ImagePickResponse;
import com.raymond.imagepick.object.LocalImageLoader;
import com.raymond.imagepick.object.ScreenSizeUtil;
import com.raymond.imagepick.object.SparseBooleanArrayParcelable;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
/**
 * GridView的adapter
 * @author Raymond Yan
 *
 */
public class ImageSelectAdapter extends BaseAdapter
{
	private Context m_ctx;
	/**
	 * 文件夹下所有图片的路径
	 */
	private ArrayList<String> m_filePath;
	/**
	 * 已选择的图片路径
	 */
	private ArrayList<String> m_filePathSelected;
	/**
	 * 存储已被选择的图片的position。SparseBooleanArray
	 */
	private SparseBooleanArrayParcelable m_selectionMap; //记录有没有被选择过
	/**
	 * 用于异步load图片
	 */
	private LocalImageLoader m_imageLoader;
	/**
	 * 回传接口，用于ImageSelectActivity更新已选数目
	 */
	private ImagePickResponse m_response;
	/**
	 * 已选图片数目，不能大于9张
	 */
	private int m_num;  //记录checked过的数目，不能大于9
	/**
	 * 设置 imageview的宽和高，以适应屏幕。
	 */
	private int m_newWidth; 
	
	public ImageSelectAdapter(Context ctx, ArrayList<String> filePath, ImagePickResponse response) 
	{
		super();
        m_ctx = ctx;
        m_filePath = filePath;
        m_selectionMap = new SparseBooleanArrayParcelable();
        m_filePathSelected = new ArrayList<String>();
        m_response = response;
        setNum(0);

        float screendensity = ScreenSizeUtil.getScreenDensity(); //屏幕的密度，像素比例：0.75/1.0/1.5/2.0
        int screenW = ScreenSizeUtil.getScreenW();	// 屏幕宽（像素，如：480px)
        int screenH = ScreenSizeUtil.getScreenH();
        m_imageLoader = new LocalImageLoader(screenW, screenH);
        
        //设置 imageview的宽和高，以适应屏幕。
        float px = 1 * screendensity;	// 水平间隔 1dp。 PX = dp x dip？
        m_newWidth = (int) (screenW-px) / 4; //设置图片的宽高为屏幕宽度的 1/4倍，一行显示4张图片
    }
	/**
	 * 清楚已选的图片路径和selectionMap存储的值
	 */
	public void ClearAll()
	{
		m_selectionMap.clear();
		m_filePathSelected.clear();
		setNum(0);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return m_filePath.size();
	}

	@Override
	public Object getItem(int position) {
		
		return m_filePath.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		final String filePath = (String) getItem(position);
		ViewHolder holder = new ViewHolder();
        if (convertView == null) 
        {
        	convertView = LayoutInflater.from(m_ctx).inflate(R.layout.item_image, null);
        	
        	holder.imageView = (ImageView) convertView.findViewById(R.id.gridview_image);
        	holder.checkBox = (CheckBox) convertView.findViewById(R.id.gridview_item_cb);
        	holder.button = (Button) convertView.findViewById(R.id.gridview_select);
        	convertView.setTag(holder);
        } 
        else 
        {
        	holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setLayoutParams(new RelativeLayout.LayoutParams(m_newWidth, m_newWidth));
        
        
        
        //tag的key必须使用id的方式定义以保证唯一，否则会出现IllegalArgumentException，以便在setOnCheckedChangeListener里面拿到当前的ImageView 和 Position
        holder.checkBox.setTag(R.id.checkbox_position, position);
        holder.checkBox.setTag(R.id.checkbox_imageview, holder.imageView);
        holder.checkBox.setOnCheckedChangeListener(new onCheckedListener());
        
        //隐形的button，为了增大check的可按范围，按下去等于checked了某个box。
        holder.button.setTag(R.id.checkbox_filepath,filePath);
        holder.button.setTag(R.id.checkbox_checkbox, holder.checkBox);
        holder.button.setOnClickListener(new onImagePickListener());
        
        //ImageView，onclick则进入大图
        holder.imageView.setTag(R.id.checkbox_position, position);
        holder.imageView.setOnClickListener(new onImageClickListener());
        //检查是否已经在m_selectionMap中checked过，有的话则setChecked
        holder.checkBox.setChecked(m_selectionMap.get(position));
        holder.imageView.setTag(filePath);
        //异步加载图片，压缩倍率为屏幕大小的1/4
        m_imageLoader.loadImage(4, filePath, holder.imageView);
        return convertView;
	}
	
	private class ViewHolder 
	{
		ImageView imageView;
		CheckBox checkBox;
		Button button;
	}
	
	public SparseBooleanArrayParcelable getSelectionMap() 
	{
        return m_selectionMap;
    }
	
	public void setSelectionMap(SparseBooleanArrayParcelable array) 
	{
        m_selectionMap = array;
    }
	
	public void clearSelectionMap() 
	{
		m_selectionMap.clear();
    }
	
	public ArrayList<String> getSelectedFilePath() 
	{
		return m_filePathSelected;
	}
	
	public void setSelectedFilePath( ArrayList<String> path )
	{
		m_filePathSelected = path;
	}
	
	public int getNum() {
		return m_num;
	}

	public void setNum(int m_num) {
		this.m_num = m_num;
	}
	/**
	 * 选择图片的CheckBoxListener
	 * @author Raymond Yan
	 *
	 */
	public class onCheckedListener implements CompoundButton.OnCheckedChangeListener
	{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			//选择的图片的position
			 Integer position = (Integer) buttonView.getTag(R.id.checkbox_position); 
			 //当前的ImageView
             ImageView image = (ImageView) buttonView.getTag(R.id.checkbox_imageview);
             m_selectionMap.put(position, isChecked);
             if (isChecked) 
             {
                 image.setColorFilter(m_ctx.getResources().getColor(R.color.image_checked_bg));
             } else 
             {
                 image.setColorFilter(null);
             }
		}
	}
	
	/**
	 * ImagePickListener，选择了图片的listener，隐形的button
	 */
	public class onImagePickListener implements Button.OnClickListener
	{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String filepath = (String) v.getTag(R.id.checkbox_filepath);
			CheckBox checkbox = (CheckBox) v.getTag(R.id.checkbox_checkbox);
			if(checkbox.isChecked())
			{
				setNum(getNum() - 1); //已选图片数目-1
            	m_filePathSelected.remove(filepath); 
            	m_response.onImageUnpicked(getNum()); //ImageSelectActivity回传接口
				checkbox.setChecked(false); //触发checkbox
			}
			else
			{
				if(getNum() >= 9)
            	{
            		m_response.onFull(); 
            		return;
            	}
				setNum(getNum() + 1);
				m_filePathSelected.add(filepath);
            	m_response.onImagePicked(getNum());
				checkbox.setChecked(true);
			}
		}
	}
	
	/**
	 * 点击图片imageview，回传给ImageSelectActivity,进入ImageViewActivity
	 * @author Raymond Yan
	 *
	 */
	public class onImageClickListener implements OnClickListener
	{
		
		@Override
		public void onClick(View v) 
		{
			Integer position = (Integer) v.getTag(R.id.checkbox_position);
			m_response.onImageShow(position);
		}
		
	}

}
