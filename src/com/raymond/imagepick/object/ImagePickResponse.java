package com.raymond.imagepick.object;

public interface ImagePickResponse 
{
	/**
	 * 选择图片
	 * @param num 图片路径的position
	 */
	void onImagePicked(int num);
	/**
	 * 取消选择图片
	 * @param num
	 */
	void onImageUnpicked(int num);
	/**
	 * 选择图片数量大于9张
	 */
	void onFull();
	/**
	 * 显示图片大图
	 * @param position 在路径list的position
	 */
	void onImageShow( int position );
}
