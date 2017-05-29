package com.janyo.janyoshare.util;

import android.content.Context;
import java.io.File;

public class CleanFileDir
{
	public static void cleanCaches(Context context)
	{
		cleanFileDir(context.getCacheDir());
	}

	public static void cleanFiles(Context context)
	{
		cleanFileDir(context.getFilesDir());
	}

	private static void cleanFileDir(File dir)
	{
		if (dir != null && dir.exists() && dir.isDirectory())
		{
			for (File item : dir.listFiles())
			{
				item.delete();
			}
		}
	}
}