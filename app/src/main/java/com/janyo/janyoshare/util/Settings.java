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
		return sharedPreferences.getBoolean(context.getString(R.string.key_auto_clean), true);
	}
}
