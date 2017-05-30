package com.janyo.janyoshare.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil
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

	public static void fileToSD(String inputPath, String fileName, String version)
	{
		File outFile = new File(Environment.getExternalStoragePublicDirectory("JanYoShare"), fileName);
		fileCopy(inputPath, outFile + "_" + version + ".apk");
	}

	private static void fileCopy(String inputPath, String outPath)
	{
		try
		{
			InputStream is = new FileInputStream(inputPath);
			FileOutputStream fileOutputStream = new FileOutputStream(outPath);
			byte[] Buff = new byte[1024];
			int fileSize = 0;
			int ReadCount = 0;
			while ((ReadCount = is.read(Buff)) != -1)
			{
				fileSize += ReadCount;
				fileOutputStream.write(Buff, 0, ReadCount);
			}
			is.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void DirExist(String dir)
	{
		String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
		File file = new File(sdcard + dir + File.separator);
		if (!file.exists())
		{
			file.mkdir();
		}
	}

	public static void Share(Context context, String uri)
	{
		//Toast.makeText(context, uri, Toast.LENGTH_SHORT).show();
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("*/*");
		share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(uri)));
		context.startActivity(Intent.createChooser(share, "分享"));
	}

	public static void doShare(Context context, String path, String name, String versionName)
	{
		fileToSD(path, name, versionName);
		Toast.makeText(context, "已提取到手机储存/JanYoShare/" + name + "_" + versionName + ".apk", Toast.LENGTH_SHORT).show();
//		try
//		{
//			if (new FileOperation(context, path, name, versionName).getOperationResult())
//				Toast.makeText(context, "拷贝" + name + "_" + versionName + "成功", Toast.LENGTH_SHORT).show();
//			else
//				Toast.makeText(context, "拷贝" + name + "_" + versionName + "失败", Toast.LENGTH_SHORT).show();
//		} catch (IOException e)
//		{
//			Toast.makeText(context, "Stream Operation Error", Toast.LENGTH_LONG).show();
//		}
		File file = new File(Environment.getExternalStoragePublicDirectory("JanYoShare"), name + "_" + versionName + ".apk");
		Share(context, file.getPath());
	}
}
