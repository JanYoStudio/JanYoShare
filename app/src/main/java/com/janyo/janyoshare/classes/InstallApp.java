package com.janyo.janyoshare.classes;

import android.graphics.drawable.Drawable;

public class InstallApp
{
	private String name;
	private String versionName;
	private String sourceDir;
	private String packageName;
	private Drawable icon;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getVersionName()
	{
		return versionName;
	}

	public void setVersionName(String versionName)
	{
		this.versionName = versionName;
	}

	public String getSourceDir()
	{
		return sourceDir;
	}

	public void setSourceDir(String sourceDir)
	{
		this.sourceDir = sourceDir;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public void setPackageName(String packageName)
	{
		this.packageName = packageName;
	}

	public Drawable getIcon()
	{
		return icon;
	}

	public void setIcon(Drawable icon)
	{
		this.icon = icon;
	}
}
