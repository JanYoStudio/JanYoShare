package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.janyo.janyoshare.R;

public class Settings
{
	private Context context;
	private SharedPreferences sharedPreferences;

	public Settings(Context context)
	{
		this.context = context;
		this.sharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
	}

	public void setAutoClean(boolean isAutoClean)
	{
		sharedPreferences.edit().putBoolean(context.getString(R.string.key_auto_clean), isAutoClean).apply();
	}

	public boolean isAutoClean()
	{
		return sharedPreferences.getBoolean(context.getString(R.string.key_auto_clean), false);
	}

	public void setUseSnackBar(boolean isUseSnackBar)
	{
		sharedPreferences.edit().putBoolean(context.getString(R.string.key_use_snack_bar), isUseSnackBar).apply();
	}

	public boolean isUseSnackBar()
	{
		return sharedPreferences.getBoolean(context.getString(R.string.key_use_snack_bar), false);
	}

	public void setDir(String dir)
	{
		sharedPreferences.edit().putString(context.getString(R.string.key_set_dir), dir).apply();
	}

	public String getDir()
	{
		return sharedPreferences.getString(context.getString(R.string.key_set_dir), "JY Share");
	}
}
