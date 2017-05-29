package com.janyo.janyoshare.util;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileOperation
{
	Context context;
	String NewFile;

	public FileOperation(Context context, String path, String newFileName, String versionName) throws IOException
	{
		this.context = context;
		newFileName = newFileName + "_" + versionName + ".apk";
		this.NewFile = newFileName;
		File src = new File(path);
		int fileSize = 0;
		int ReadCount = 0;

		InputStream is = new FileInputStream(src);
		FileOutputStream fos = context.openFileOutput(newFileName, Context.MODE_PRIVATE);
		byte[] Buff = new byte[1024];

		while ((ReadCount = is.read(Buff)) != -1)
		{
			fileSize += ReadCount;
			fos.write(Buff, 0, ReadCount);
		}
		is.close();
	}

	public boolean getOperationResult()
	{
		return context.getDir(NewFile, Context.MODE_PRIVATE).exists();
	}

	/*public void DelFile(String path)
	{
		context.deleteFile(NewFile);
	}*/
}