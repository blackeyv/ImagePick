package com.raymond.imagepick.adapter;

import java.io.File;
import java.util.ArrayList;

import com.raymond.imagepick.R;
import com.raymond.imagepick.object.ImageParentPathItem;
import com.raymond.imagepick.object.LocalImageLoader;
import com.raymond.imagepick.object.ScreenSizeUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 用于 spinner的ArrayAdapter
 * @author Raymond Yan
 *
 */
public class ImageParentPathAdapter extends ArrayAdapter<ImageParentPathItem>
{
	private Context m_ctx;
	private ArrayList<ImageParentPathItem> m_arraylist;
	private int m_res ;
	private LocalImageLoader m_loader;
	
	/**
	 * 
	 * @param context
	 * @param resource Spinner 的下拉显示layout file，
	 * @param objects ImageParentPathItem list,存储文件夹的 路径，照片数目，和第一张照片的路径
	 */
	public ImageParentPathAdapter(Context context, int resource,
			ArrayList<ImageParentPathItem> objects) 
	{
		super(context, resource, objects);
		m_ctx = context;
		m_arraylist = objects;
		m_res = resource;
        int screenW = ScreenSizeUtil.getScreenW();
        int screenH = ScreenSizeUtil.getScreenH();
        m_loader = new LocalImageLoader(screenW, screenH);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return super.getCount();
	}

	/**
	 * getView 显示的是，当spinner 没有被点击时，显示的VIEW应该是怎么样。没有被点击是，只显示文件夹的名字
	 * getDropDownView 显示的是 当点击后下拉菜单的样式
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		// TODO Auto-generated method stub
		
		ViewHolder ivHolder = new ViewHolder();
		if (convertView == null) {
			convertView = LayoutInflater.from(m_ctx).inflate(R.layout.list_item_filepath_title, parent,
					false);
			ivHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.list_item_filepath_name);
			convertView.setTag(ivHolder);
		} else {
			ivHolder = (ViewHolder) convertView.getTag();
		}
		
		ivHolder.tvTitle.setText(getPathNameToShow(m_arraylist.get(position).getPathName())); //spinner没有被选中时，只显示名字。
		return convertView;
	}
	
	/**
	 * 被点击时的下拉菜单的view，有文件夹第一张图片，文件夹名字以及图片的数量
	 */

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder ivHolder = new ViewHolder();
		if (convertView == null) {
			convertView = LayoutInflater.from(m_ctx).inflate(m_res, parent,
					false);
			ivHolder.ivHeader = (ImageView) convertView
					.findViewById(R.id.list_item_image);
			ivHolder.tvTitle = (TextView) convertView
					.findViewById(R.id.list_item_filename);
			ivHolder.tvCount = (TextView) convertView
					.findViewById(R.id.list_item_count);
			convertView.setTag(ivHolder);
		} else {
			ivHolder = (ViewHolder) convertView.getTag();
		}
		
		ivHolder.tvTitle.setText(getPathNameToShow(m_arraylist.get(position).getPathName()));
		ivHolder.tvCount.setText(String.valueOf(m_arraylist.get(position).getFileCount()));
		ivHolder.ivHeader.setTag(m_arraylist.get(position).getFirstImagePath());
		m_loader.loadImage(4, m_arraylist.get(position).getFirstImagePath(), ivHolder.ivHeader);
		return convertView;
	}

	static class ViewHolder 
	{
		public ImageView ivHeader;
		public TextView tvTitle;
		public TextView tvCount;
	}
	
	 /**根据完整路径，获取最后一级路径，并拼上文件数用以显示。*/
    public String getPathNameToShow(String absolutePath) 
    {
        int lastSeparator = absolutePath.lastIndexOf(File.separator);
        return absolutePath.substring(lastSeparator + 1);
    }


}
