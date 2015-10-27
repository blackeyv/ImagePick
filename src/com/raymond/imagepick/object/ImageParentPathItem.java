package com.raymond.imagepick.object;

/**
 * 存储图片文件夹的文件路径，图片数目以及第一张图片的地址。
 * @author Raymond Yan
 *
 */

public class ImageParentPathItem 
{		

	private String pathName;
	private int fileCount;
	private String firstImagePath;
	/**
	 *  
	 * @param pathName   文件名路径
	 * @param fileCount   文件夹下照片数目
	 * @param firstImagePath  文件夹下第一张照片的地址
	 */
	public ImageParentPathItem(String pathName, int fileCount,
			String firstImagePath) 
	{
		this.pathName = pathName;
		this.fileCount = fileCount;
		this.firstImagePath = firstImagePath;
	}

	public String getPathName() 
	{
		return pathName;
	}

	public void setPathName(String pathName) 
	{
		this.pathName = pathName;
	}

	public int getFileCount() 
	{
		return fileCount;
	}

	public void setFileCount(int fileCount) 
	{
		this.fileCount = fileCount;
	}

	public String getFirstImagePath() 
	{
		return firstImagePath;
	}
	
	/**
	 * 设置文件夹中第一张图片的路径
	 * @param firstImagePath
	 */
	public void setFirstImagePath(String firstImagePath) 
	{
		this.firstImagePath = firstImagePath;
	}

	@Override
	public String toString() 
	{
		return "SelectImgGVItem{" + "pathName='" + pathName + '\''
				+ ", fileCount=" + fileCount + ", firstImagePath='"
				+ firstImagePath + '\'' + '}';
	}
}
