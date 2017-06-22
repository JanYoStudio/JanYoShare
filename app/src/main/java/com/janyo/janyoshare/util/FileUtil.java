package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil
{
	public static boolean cleanFileDir(String dir)
	{
		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + dir + File.separator);
		if (file.exists() && file.isDirectory())
		{
			for (File item : file.listFiles())
			{
				//noinspection ResultOfMethodCallIgnored
				item.delete();
			}
			return true;
		}
		return false;
	}

	public static int fileToSD(String inputPath, String fileName, String version, String dir)
	{
		File outFile = new File(Environment.getExternalStoragePublicDirectory(dir), fileName);
		return fileCopy(inputPath, outFile + "_" + version + ".apk");
	}

	private static int fileCopy(String inputPath, String outPath)
	{
		int code = -1;
		try
		{
			InputStream is = new FileInputStream(inputPath);
			FileOutputStream fileOutputStream = new FileOutputStream(outPath);
			byte[] Buff = new byte[1024];
			int ReadCount;
			while ((ReadCount = is.read(Buff)) != -1)
			{
				fileOutputStream.write(Buff, 0, ReadCount);
			}
			is.close();
			code = 1;
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		return code;
	}

	public static boolean isDirExist(String dir)
	{
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		File file = new File(sdcard + dir + File.separator);
		return file.exists() || file.mkdir();
	}

	private static void Share(Context context, File file)
	{
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		context.startActivity(Intent.createChooser(share, "分享"));
	}

	public static void doShare(Context context, String name, String versionName, String dir)
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(dir), name + "_" + versionName + ".apk");
		Share(context, file);
	}
}
