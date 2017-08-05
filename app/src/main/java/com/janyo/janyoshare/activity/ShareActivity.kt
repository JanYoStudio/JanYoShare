package com.janyo.janyoshare.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.janyo.janyoshare.classes.TransferFile
import com.mystery0.tools.FileUtil.FileUtil
import com.mystery0.tools.Logs.Logs
import java.io.File
import java.io.FileInputStream

class ShareActivity : AppCompatActivity()
{
	private val TAG = "ShareActivity"
	override fun onCreate(savedInstanceState: Bundle?)
	{
		super.onCreate(savedInstanceState)
		val action = intent.action
		val type = intent.type
		if (action == Intent.ACTION_SEND && type != null)
		{
			val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
			Logs.i(TAG, "onCreate: " + uri)
			val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
			val cursor = contentResolver.query(uri, null, null, null, null)
			val fileNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
			cursor.moveToFirst()
			val transferFile = TransferFile()
			transferFile.fileName = cursor.getString(fileNameIndex)
			transferFile.fileUri = uri.toString()
//			transferFile.fileIconPath = installApp.iconPath
			transferFile.fileSize = parcelFileDescriptor.statSize
			cursor.close()
			val intent = Intent(this, FileTransferConfigureActivity::class.java)
			val bundle = Bundle()
			bundle.putSerializable("app", transferFile)
			intent.putExtra("action", 1)
			intent.putExtra("app", bundle)
			startActivity(intent)
			finish()
		}
		else
		{
			Toast.makeText(this, "无效的文件！", Toast.LENGTH_SHORT)
					.show()
		}
	}
}