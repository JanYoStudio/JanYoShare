package com.janyo.janyoshare.util;

import android.os.Environment;

import java.io.File;

public class DirExist
{

	public DirExist(String dir)
	{
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		File file = new File(sdcard + dir + File.separator);
		if (!file.exists())
		{
			file.mkdir();
		}
	}
}