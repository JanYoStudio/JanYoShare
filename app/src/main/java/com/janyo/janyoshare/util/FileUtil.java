package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

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
				item.deleteOnExit();
			}
			return true;
		}
		return false;
	}

	private static int fileToSD(String inputPath, String fileName, String version)
	{
		File outFile = new File(Environment.getExternalStoragePublicDirectory("JanYoShare"), fileName);
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

	private static void Share(Context context, String uri)
	{
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(uri)));
		context.startActivity(Intent.createChooser(share, "分享"));
	}

	public static void doShare(Context context, String path, String name, String versionName)
	{
		if (fileToSD(path, name, versionName) == 1)
		{
			Toast.makeText(context, "已提取到手机储存/JanYoShare/" + name + "_" + versionName + ".apk", Toast.LENGTH_SHORT)
					.show();
			File file = new File(Environment.getExternalStoragePublicDirectory("JanYoShare"), name + "_" + versionName + ".apk");
			Share(context, file.getPath());
		} else
		{
			Toast.makeText(context, "导出到SD卡出错！", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
